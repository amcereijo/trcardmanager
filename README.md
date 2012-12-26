TRCardManager
==================================================

Small Android app to manage Ticket Restaurant Cards

The application allows ticket restaurant card's user to:
  * check balance and movements 
  * change account password
  * recover account password
  * update card number
  * search for a restaurant at selected place or around current position. It lets you use google maps/navigation or Waze to go to the selected restaurant
  
If you download the source code and you want to run it, you have to:

  * for a map view, you must get a google maps id and put it in "/res/values/resources.xml" and "/res/values-es/resources.xml" files for the value of "MAP_ID"
  * for ads, you have tow options:
 
    1) get a admob id and put it in "/res/values/resources.xml" and "/res/values-es/resources.xml" files for the value of "unidId". In the same files you should put for the value of "dev_device" your device id if you are going to use your device for development.

    2) remove "<com.google.ads.AdView android:layout_width="wrap_content"..." tag in "/res/layout/main.xml" file
    
    