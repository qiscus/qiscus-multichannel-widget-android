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