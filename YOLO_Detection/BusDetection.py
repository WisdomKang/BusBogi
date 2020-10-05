import cv2 
import numpy as np
import threading
import time
import pytesseract
from PIL import Image
import FCM_send as push

import argparse


class Detection(threading.Thread):

    def __init__(self, bus_list, user_token):
        threading.Thread.__init__(self)
        self.dec_bus_list = bus_list
        self.user_token = user_token

    def run(self):
        #Homography를 이용한 거리를 측정
        #실측으로 지면의 좌표와 영상의 투영되는 좌표를 기입한다.
        real_point = np.array([ [0,0] , [50,0] , [0, 200], [50,200]])
        image_point = np.array( [[684,550], [841,530],[528,293],[640,285]])
        H, status = cv2.findHomography(image_point, real_point, cv2.RANSAC)

        #YOLO 가중치 Load
        net = cv2.dnn.readNet('yolo_python/yolo-data/yolov3-tiny-bus-200918.weights', 'yolo_python/yolo-data/yolov3-tiny_custom.cfg')

        layer_names = net.getLayerNames()
        output_layers = [layer_names[i[0] - 1] for i in net.getUnconnectedOutLayers()]

        detection_time = 0
        detection_cnt = 0
        stop_position = 0


        #객체 인식 루프
        while True:
            cap = cv2.VideoCapture("http://192.168.12.17/html/cam_pic_new.php")
            ret ,img  = cap.read()
            height, width, channels = img.shape

            blob = cv2.dnn.blobFromImage(img, 0.00392, (416, 416), (0, 0, 0), True, crop=False)
            net.setInput(blob)
            outs = net.forward(output_layers)

            confidences = []
            boxes = []

            #찾은 객체들에서 원하는 정확도 이상의 객체를 추려냄.
            #Box좌표 추출
            for out in outs:
                for detection in out:
                    scores = detection[5:]
                    class_id = np.argmax(scores)
                    confidence = scores[class_id]
                    if confidence > 0.7:
                        # Object detected
                        center_x = int(detection[0] * width)
                        center_y = int(detection[1] * height)
                        w = int(detection[2] * width)
                        h = int(detection[3] * height)

                        # Rectangle coordinates
                        x = int(center_x - w / 2)
                        y = int(center_y - h / 2)

                        boxes.append([x, y, w, h])
                        confidences.append(float(confidence))

            #한 객체에 여러개의 박스가 쳐지는것을 걸러냄
            indexes = cv2.dnn.NMSBoxes(boxes, confidences, 0.1, 0.4)

            font = cv2.FONT_HERSHEY_PLAIN

            #박스, 라벨 및 거리 표시
            for i in range(len(boxes)):
                if i in indexes:
                    x, y, w, h = boxes[i]
                    centerPoint = np.array([ x+w/2, y+h, 1])

                    #Homography 이용 하여 특정 지점에서 거리 계산
                    ground_point = np.dot(H, centerPoint)
                    xd , yd = ground_point[0]/ground_point[2] , ground_point[1]/ground_point[2]

                    cur_position = int(yd)/ 10

                #이전위치와 비교하여 일정 시간 이상 위치변환이 없을때를 이용 하여 정차 상태 확인
                    if stop_position - cur_position < 0.5:
                        detection_cnt += 1
                    else:
                        detection_cnt = 0

                    stop_position = cur_position

                    cut_image = img[y:(y+h), x:(x+w) ]
                    bus_number = 273

                    is_stop = ""
                    if detection_cnt > 4 and bus_number in self.dec_bus_list:
                        text = str(bus_number) +"번 버스가 약 " +  str(stop_position) + "미터에 정차하였습니다."
                        push.send_push(user_id, text)
                        is_stop = " stop"
                        return
                    
                    label = "distance : " + str(cur_position) + "m" +is_stop

                    color = (11,180,12)
                    cv2.rectangle(img, (x, y), (x + w, y + h), color, 2)
                    cv2.putText(img, label, (x, y - 10), font, 1.5, (255, 255, 255),2)

            
            #서버 환경에서 주석 처리
            cv2.imshow("test",img)

            if cv2.waitKey(10) > 0:
                break


    def extract_number(self, cut_image):
        resize_bus = cv2.resize(cut_image, None , fx=2.0, fy=2.0, interpolation=cv2.INTER_AREA)

        # GaussianBlur
        dst2 = cv2.GaussianBlur(resize_bus, (5, 5), 0)
        #
        # # Median Blur
        # dst3 = cv2.medianBlur(img, 7)

        # Bilateral Filtering
        dst4 = cv2.bilateralFilter(dst2, 3, 5, 50)

        # from color to grey
        gray_img = cv2.cvtColor(dst4, cv2.COLOR_BGR2GRAY)

        canny = cv2.Canny(gray_img, 70 , 130)

        cv2.imshow("test", canny)
        cv2.waitKey(0)

        # # Threshold
        # ret, thresh = cv2.threshold(result0, 10, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        # cv2.imshow("Threshold - 4", thresh)
        # cv2.waitKey(0)

        contours, hierachy = cv2.findContours(canny, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
        
        drawCont = cv2.drawContours(gray_img, contours, -1, (0,255,0), 3)


        for i in range(len(contours)):
            x, y, w, h = cv2.boundingRect(contours[i])
            aspect_ratio = float(w) / h
            test = cv2.contourArea(contours[i])
            print( test )
            if aspect_ratio > 1.0 and test > 100:
                resize_bus = cv2.rectangle(resize_bus,(x,y),(x+w,y+h),(0,255,0),2)

            # image = Image.fromarray(res)

            # config = ('--oem 3 --psm 6 outputbase digits')

            # start = time.time()
            # text = pytesseract.image_to_string(image, config=config)
            # result = ''
            # for i in range(0, len(text)):
            #     if str.isdigit(text[i]):
            #         result+= text[i]
            # return result

        cv2.imshow('test', resize_bus)
        cv2.waitKey(0)
        

if __name__ == "__main__":
    print("Detection Start!")
    parser = argparse.ArgumentParser()
    parser.add_argument('--user')
    parser.add_argument('--bus', nargs='*')
    args = parser.parse_args()

    user_id = args.user
    bus_list = args.bus

    for i, data in enumerate(bus_list):
        bus_list[i] = int(data)
    print( user_id )
    print( bus_list )

    thread = Detection(bus_list, user_id)
    thread.start()
