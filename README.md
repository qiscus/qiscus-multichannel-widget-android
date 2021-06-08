# Qiscus Multichannel Widget Android

Qiscus Multichannel Widget Android is official [sdk](https://github.com/qiscus/qiscus-sdk-android/tree/multiple-appid) widget to implement [Qiscus Multichannel Customer](https://www.qiscus.com/customer-service-chat) to your app.

## Installation

Add this URL reference in your project build.gradle
```
allprojects {
   repositories {
       ...
       maven {
           url "https://artifactory.qiscus.com/artifactory/qiscus-library-open-source"
       }
   }
}
```

Then add this to your app build.gradle

```
dependencies {
    ...
    implementation 'com.qiscus.integrations:multichannel-widget:0.0.20-v3'
}
```

## Setup

You need to initiate your `APP ID` for your chat app before carry out to authentication. This initialization only needs to be done once in the app lifecycle.

```
class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val qiscusCore = QiscusCore()
        
        MultichannelWidget.setup(this, qiscusCore, APPID, LOCALPREFKEY)

        ...
    }
}
```
**APPID** : Multichannel APPID, you can get your it from [here](https://multichannel.qiscus.com)

**LOCALPREFKEY** : local data identifier

You can customize your configuration like logging, notification listener, room title, room subtitle, etc using

```
val config = MultichannelWidgetConfig
               .setEnableLog(BuildConfig.DEBUG)
               .setNotificationListener(null)
               .setRoomTitle("Custom Title")
               .setRoomSubtitle("Custom Subtitle")

MultichannelWidget.setup(context, qiscusCore, APPID, config, LOCALPREFKEY)
```

## Initiate Chat
Start chatting can be done in single method
```java
MultichannelWidget.instance.initiateChat(context, userId, email, avatar, extras, userProperties)

```
**name** (String) : Username

**userId** (String) : A user identifier that will be used to identify a user and used whenever another user need to chat with this user. It can be anything, whether is user's email, your user database index, etc. As long as it is unique and a string.

**avatar** (String, optional) : user avatar, if empty it will use the default avatar

**extras** (JSONObject, optional) : extra data

**userProperties** (Map<String, String>, optional) : user properties for multichannel

## Customization

There are some additional configuration that you can change

No | Config | Desciption 
--- | --- | --- 
1 | setEnableLog | Set true if you want to show multichannel widget log 
2 | setNotificationListener | Set your custom notification handler
3 | setEnableNotification | Enable app notification 
4 | setRoomTitle | Set Room Title 
5 | setRoomSubtitle | Set Room Subtitle 
6 | setHideUIEvent | Show / Hide System Event 
7 | setVideoPreviewOnSend | Show / Hide video preview after user select video in attachment 
8 | ~~setHardcodedAvatar~~ | Set your user default avatar (Deprecated) 

And for color configuration you just need to change it from your app [color res](https://developer.android.com/guide/topics/resources/more-resources#Color)

No | Color Name | Desciption 
--- | --- | --- 
1 | qiscus_statusbar_mc | Chat room status bar color
2 | qiscus_appbar_mc | Chat room appbar bar color
3 | qiscus_accent_mc | Widget accent color
4 | qiscus_text_reply_mc | Reply line color
5 | qiscus_send_button_mc | Send button color
6 | qiscus_pick_image_mc | Pick image icon color 
7 | qiscus_pick_doc_mc | Pick document icon color
8 | qiscus_title_mc | Chat room title color
9 | qiscus_subtitle_mc | Chat room subtitle color
10 | qiscus_back_icon_mc | Chat room back icon color
11 | qiscus_left_bubble_mc | Left (Friend) bubble chat color
12 | qiscus_right_bubble_mc | Right (Me) bubble chat color
13 | qiscus_left_bubble_text_mc | Left (Friend) bubble chat text color
14 | qiscus_right_bubble_text_mc | Right (Me) bubble chat text color
15 | qiscus_send_container_mc | Message box container color
16 | read_message_mc | Message status color if readed
17 | pending_message_mc | Message status color when pending
18 | jupuk_primary | Image & Doc picker appbar color
19 | jupuk_primary_dark | Image & Doc picker statusbar color
20 | jupuk_accent | Image & Doc picker accent color

## Proguard
if your app using [Proguard](https://www.guardsquare.com/proguard), make sure you add Proguard Rules of Qiscus from [Qiscus Proguard Rules](https://github.com/qiscus/qiscus-sdk-android/blob/master/app/proguard-rules.pro) to your Proguard Rules

## Troubleshoot
If you facing error like this
```
More than one file was found with OS independent path 'META-INF/rxjava.properties'
```
Add this to your app build.gradle

```
android {
    .....
    .....
    
    packagingOptions {
        exclude 'META-INF/rxjava.properties'
    }
} 
```