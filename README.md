# Android Multichannel-widget

This is unofficial sdk for implement [Qiscus multichannel](https://www.qiscus.com/customer-service-chat) to your android apps. Your apps must using androidx and requires minimum Android API 16 (Jelly Bean).

## Install
First, you need to add URL reference in your .gradle project.

 ```
 allprojects {
    repositories {
        ...
        maven { url "https://dl.bintray.com/qiscustech/maven" }
        maven {
            url "https://artifactory.qiscus.com/artifactory/qiscus-library-open-source"
        }
    }
}
 ```

Then in your app build.gradle you need add this.

```
dependencies {
    ...
    implementation ('com.qiscus.integrations:multichannel-widget:0.0.2')
}
```


## Setup

First, initialize Multichannle in your application class. In this part you will need Qiscus Multichannel AppId. You can get the AppId [here](https://multichannel.qiscus.com/).

 ```
/**
* appId (required) : your qiscus multichannel appid 
*/

MultichannelWidget.init(this, appId)
```

If you need some custom configuration like logging, notification, you can using this method

```
/**
* appId (required) : your qiscus multichannel appid 
* config (optional) : custom config for multichannel widget
*/
val config = MultichannelWidgetConfig
                .setEnableLog(true)
                .setNotificationListener(null)

MultichannelWidget.init(this, appId, config)
```

## Initiate chat

Start chatting can be done in single method, here is how you can do that

```
/**
* context (required) : context activity
* nama (required) : username
* userid (required) : userId from user
* avatar (optional) : user avatar
* extras (optional) : extra data (json)
*/
MultichannelWidget.instance.initiateChat(context, name, userId, avatar, extras)
```


## Troubleshoot
If you facing error like this

```
More than one file was found with OS independent path 'META-INF/rxjava.properties'
```

Just add this code in your app build.gradle

```
android {
    .....
    .....
    
    packagingOptions {
        exclude 'META-INF/rxjava.properties'
    }
} 
```
