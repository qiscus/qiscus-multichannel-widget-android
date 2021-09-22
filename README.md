# Qiscus Multichannel Widget Android
Requirements

* Android Studio v4.1.3 or latest
* Min sdk 16
* Java 8
* Kotlin v1.3.50
* build gradle 4.0.1

Installation

Add this URL reference in your project build.gradle

allprojects {
   repositories {
       ...
       maven {
           url "https://artifactory.qiscus.com/artifactory/qiscus-library-open-source"
       }
   }
}

Then add this to your app build.gradle

dependencies {
    ...
    implementation 'com.qiscus.multichannel:multichannel-widget:2.0.0-beta.1'
}

How To Use

Initialization

In order to use QiscusMultichannelWidget, you need to initialize it with your AppID (YOUR_APP_ID). Get more information to get AppID from here (https://multichannel.qiscus.com/)

class SampleApp : Application() {

    lateinit var qiscusMultichannelWidget: QiscusMultichannelWidget

    override fun onCreate() {
        super.onCreate()

        val qiscusCore = QiscusCore()
        
        qiscusMultichannelWidget = QiscusMultichannelWidget.setup(this, qiscusCore, "YOUR_APP_ID", "LOCALPREFKEY")

        ...
    }
}

LOCALPREFKEY : local data identifier
After the initialization, you can access all the widget's functions.

Set The User

Set UserId before start the chat, this is mandatory.

qiscusMultichannelWidget.setUser(id: "UserId", displayName: "Cus Tom R", avatarUrl: "https://customer.avatar-url.com (https://customer.avatar-url.com/)")

Get Login Status

You can check whether the user has already logged in.

qiscusMultichannelWidget.isLoggedIn()

Start Chat

Use this function to start a chat.

qiscusMultichannelWidget.initiateChat()
    .initiateAndOpenChatRoom(this)

withUserProperties(Map<String, String>, optional) : user properties for multichannel
showLoadingWhenInitiate(Boolean) : set true if you want to show Multichannel Widget default loading page
initiateAndOpenChatRoom (Context) : method to initiate chat and open Multichannel Widget Chat Room Activity

Customization

We provide several functions to customize the User Interface.

Config

Title	Description
setRoomTitle	Set room name base on customer's name or static default.
setRoomSubTitle	
	setRoomSubTitle(RoomSubtitle.Enabled)	Set enable room sub name by the system.
	setRoomSubTitle(RoomSubtitle.Disabled)	Set disable room sub name.
	setRoomSubTitle(RoomSubtitle.Editable, "Custom subtitle")	Set enable room sub name base on static default.
setHideUIEvent	Show/hide system event.
setAvatar	
	setAvatar(Avatar.Enable)	Set enable avatar and name
	setAvatar(Avatar.Disabled)	Set disable avatar and name
setEnableNotification	Set enable app notification.

Color

No	Title	Description
1	setNavigationColor	Set navigation color.
2	setSendContainerColor	Set icon send border-color.
3	setFieldChatBorderColor	Set field chat border-color.
4	setSendContainerBackgroundColor	Set send container background-color.
5	setNavigationTitleColor	Set room title, room subtitle, and back button border color.
6	setSystemEventTextColor	Set system event text and border color.
7	setLeftBubbleColor	Set left bubble chat color (for: Admin, Supervisor, Agent).
8	setRightBubbleColor	Set right bubble chat color (Customer).
9	setLeftBubbleTextColor	Set left bubble text color (for: Admin, Supervisor, Agent).
10	setRightBubbleTextColor	Set right bubble text color (Customer).
11	setTimeLabelTextColor	Set time text color.
12	setTimeBackgroundColor	Set time background color.
13	setBaseColor	Set background color of the room chat.
14	setEmptyTextColor	Set empty state text color.
15	setEmptyBackgroundColor	Set empty state background color.

Push Notification

Follow these steps to set push notifications on your application

1. *Setup Firebase to Your Android App*

If you already have setup Firebase in your Android app, you can skip this step and go to next step which is *Generate FCM Server key*. Otherwise, you can setup Firebase to your Android app by following these steps (https://firebase.google.com/docs/cloud-messaging/android/client).

1. *Get FCM Server Key in Firebase Console*

You can get FCM Server Key by following these steps:

* Go to Firebase Console (https://console.firebase.google.com/)
* Click your *projects* to see the overview your 

[Image: image.png]
* On the top of left panel, click the *gear icon* on Project Overview menu. From the drop-down menu, click *Project Settings*.

[Image: image.png]
* Click the *Cloud Messaging* tab under *Settings*. On the *Project Credentials*, find and copy your *Server Key.*

[Image: image.png]
1. *Setup FCM Server Key in The (https://documentation.qiscus.com/chat-sdk-android/push-notifications#step-3-setup-fcm-server-key-in-the-qiscus-dashboard) Qiscus Multichannel Dashboard*

You can set FCM Secret Key by following these steps:

* Go to Qiscus Multichannel Chat page (https://multichannel.qiscus.com/) to register your email
* Log in to Qiscus Multichannel Chat with yout email and password
* Go to ‘Setting’ menu on the left bar
* Look for ‘Notification’
* Click Android's Customer Widget Push Notification

[Image: multichannel_notif_fcm_setting.png]
* In the Android (FCM Server Key) section, click *+Add FCM Server Key* to add your FCM Server Key,
* Paste FCM Server Key value and click *Save change*



*NOTE*
One App Id can only be associated with one FCM Project, *make sure* the FCM Server keys are from the same FCM Project, If you already put multiple FCM server keys but they are different FCM project, then our system deletes the related device token and the effect you will not receive FCM notification.

1. *Register Your FCM Token to Qiscus Multichannel Widget*

* First you need to enable FCM for your app by calling configuration, for example:

val config = QiscusMultichannelWidgetConfig()
    .setEnableNotification(true) // default is true
    .setNotificationListener(object : MultichannelNotificationListener {
            
            override fun handleMultichannelListener(context: Context?, qiscusComment: QMessage?) {
                // show your notification here
            }
            
     })
    .setNotificationIcon(R.drawable.ic_notification)

* set configuration before calling QiscusMultichannelWidget.setup(), for example:

// input the configuration 
QiscusMultichannelWidget.setup(application, qiscusCore, "YOUR_APP_ID", config, color, "LOCALPREFKEY")

* To enable FCM in *ChatConfig*, you need to register FCM token to notify Qiscus Multicahnnel Widget, for example:

class FirebaseServices : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        QiscusMultichannelWidget.instance.registerDeviceToken(
            qiscusCore, newToken
        )
    }
}

* You need to make sure every time open the app, the FCM token always needs to be registered in Qiscus  Multicahnnel Widget. To retrieve the current FCM token, you can see below code:

 FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("Qiscus", "getCurrentDeviceToken Failed : " + task.exception)
                    return@OnCompleteListener
                }
                if (task.result != null) {
                    val currentToken = task.result!!.token
                    QiscusMultichannelWidget.instance.registerDeviceToken(
                        qiscusCore, currentToken
                    )
                }
            })

* Add the *service.FirebaseServices* in Manifest, for example:

<service android:name="com.qiscus.multichannel.sample.widget.service.FirebaseServices">
   <intent-filter>
       <action android:name="com.google.firebase.MESSAGING_EVENT" />
   </intent-filter>
</service>

*NOTE*
Make sure always to register FCM token when open the app

1. *Handle Incoming Message From Push Notification*

After registering your FCM token, you will get data from FCM Qiscus  Multicahnnel Widget, you can handle by using isMultichannelMessage() function, for example


class FirebaseServices : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (QiscusMultichannelWidget.instance.isMultichannelMessage(
                remoteMessage, allQiscusCore
            )
        ) {
            return
        }
    }
}

Proguard

if your app using Proguard (https://www.guardsquare.com/proguard), make sure you add Proguard Rules of Qiscus from Qiscus Proguard Rules (https://github.com/qiscus/qiscus-sdk-android/blob/master/app/proguard-rules.pro) to your Proguard Rules (https://github.com/qiscus/qiscus-multichannel-widget-android/blob/develop/app/proguard-rules.pro)

Troubleshoot

If you facing error like this


More than one file was found with OS independent path 'META-INF/rxjava.properties'

Add this to your app build.gradle


android {
    .....
    .....
    
    packagingOptions {
        exclude 'META-INF/rxjava.properties'
    }
} 

How to Run the Example

1. *Get your APPID*

* Go to Qiscus Multichannel Chat page (https://multichannel.qiscus.com/) to register your email
* Log in to Qiscus Multichannel Chat with yout email and password
* Go to ‘Setting’ menu on the left bar
* Look for ‘App Information’
* You can find APPID in the App Info 

1. *Activate Qiscus Widget Integration*

* Go to ‘Integration’ menu on the left bar
* Look for ‘Qiscus Widget’
* Slide the toggle to activate the Qiscus widget

1. *Set YOUR_APP_ID in the Example*

* Open SampleApp.kt
* Replace the appId with YOUR_ APP_ID (step 1)

qiscusMultichannelWidget = QiscusMultichannelWidget.setup(application, qiscusCore, "YOUR_APP_ID", "LOCALPREFKEY")

1. *Start Chat*

The Example is ready to use. You can start to chat with your agent from the Qiscus Multichannel Chat dashboard.

```
