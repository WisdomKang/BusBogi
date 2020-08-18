#hcitool dev
export BLUETOOTH_EDVICE=hci0

#bluetooth advertise 시에 고정으로 앞에 붙는 값들이다. IBeacon 규격을 사용하고 있다.
#OGF = Operation Group Field = Bluetooth Command Group = 0x08
#OCF = Operation Command Field = HCI_LE_Set_Advertising_Data = 0x0008
#No. Significant Data Octets (Max of 31) = 1E (Decimal 30)
#iBeacon Prefix (Always Fixed) = 02 01 1A 1A FF 4C 00 02 15

export OGF="0x08"
export OCF="0x0008"
export IBEACONPROFIX="1E 02 01 1A 1A FF 4C 00 02 15"

#UUID 부분이 비콘을 식별하게 해주는 ID 부분
#사용자가 원하는데로 수정해서 사용한다.
#uuidgen  could gerenate uuid
export UUID="4a 4e ce 60 7e b0 11 e4 b4 a9 08 00 20 0c 9a 66"
export MAJOR="00 02"
export MINOR="00 01"
export POWER="C5 00"

# initialize device
sudo hciconfig $BLUETOOTH_DEVICE up
# disable advertising
sudo hciconfig $BLUETOOTH_DEVICE noleadv
# stop the dongle looking for other Bluetooth devices
sudo hciconfig $BLUETOOTH_DEVICE noscan

sudo hciconfig $BLUETOOTH_DEVICE leadv3

#비콘 신호 송신 시작
sudo hcitool -i $BLUETOOTH_DEVICE cmd $OGF $OCF $IBEACONPROFIX $UUID $MAJOR $MINOR $POWER

#송신 주기 설정 1번째 2번째 A0 00 , A0 00 이 16진수로 160 BLE에서 granularity는 0.625라서 
#160 * 0.625 = 100 (ms) 가 되어 송신 주기가 된다.
sudo hcitool -i $BLUETOOTH_DEVICE cmd 0x08 0x0006 A0 00 A0 00 00 00 00 00 00 00 00 00 00 07 00
sudo hcitool -i $BLUETOOTH_DEVICE cmd 0x08 0x000a 01

echo "complete"