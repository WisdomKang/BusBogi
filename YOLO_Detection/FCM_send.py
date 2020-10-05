import requests
import json

from oauth2client.service_account import ServiceAccountCredentials

scopes = ['https://www.googleapis.com/auth/firebase.messaging']

def _get_access_token():
    credentials = ServiceAccountCredentials.from_json_keyfile_name('yolo_python/busbogi-81ed6-firebase-adminsdk-n4n9p-c5cd738e87.json', scopes)
    access_token_info = credentials.get_access_token()
    return access_token_info.access_token

def send_push(user_token, msg):
    project_id = "busbogi-81ed6"
    device_token = user_token

    url = "https://fcm.googleapis.com/v1/projects/" + project_id + "/messages:send"

    payload = {
        "message": {
            "token":device_token,
            "notification": {
                "body": msg
            }
        }
    }

    headers = {
        "Authorization": "Bearer " + _get_access_token(),
        "Content-Type": "application/json; UTF-8",
    }
    r = requests.post(url, data=json.dumps(payload), headers=headers)
