# Android Multichannel-widget

This is unofficial sdk for implement [Qiscus multichannel](https://www.qiscus.com/customer-service-chat) to your android apps. Your apps must using androidx and requires minimum Android API 16 (Jelly Bean).

## Setup APPID
In this part you will need Qiscus Multichannel AppId. You can get the AppId [here](https://multichannel.qiscus.com/).

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
    implementation ('com.qiscus.integrations:multichannel-widget:0.0.15-v3')
}
```


## Setup

First, create file ConstCore.kt

```
object ConstCore {
    private var qiscusCore1: QiscusCore? = null
    private var qiscusCore2: QiscusCore? = null
    fun setCore(){
        qiscusCore1 = QiscusCore()
        qiscusCore2 = QiscusCore()
    }

    fun qiscusCore1(): QiscusCore {
       return this.qiscusCore1!!
    }

    fun qiscusCore2(): QiscusCore {
        return this.qiscusCore2!!
    }

    fun allQiscusCore() : MutableList<QiscusCore> {
        var qiscusCores: MutableList<QiscusCore> = ArrayList()
        if (qiscusCore1 != null) {
            qiscusCores.add(ConstCore.qiscusCore1())
        }

        if (qiscusCore2 != null) {
            qiscusCores.add(ConstCore.qiscusCore2())
        }

        return qiscusCores
    }
}
```

Then, initialize Multichannel in your application class. In this part you will need Qiscus Multichannel AppId. You can get the AppId [here](https://multichannel.qiscus.com/).

 ```
 //set just 1 in 1 lifecircle
 ConstCore.setCore()

/**
* qiscusCore (required) : qiscusCore1 or qiscusCore2
* appId (required) : your qiscus multichannel appid
* localPrefKey (required) : uniq local data can random
*/

MultichannelWidget.setup(this, ConstCore.qiscusCore1(),"yourAppId","user1")
```

If you need some custom configuration like logging, notification, you can using this method

```
/**
* qiscusCore (required) : qiscusCore1 or qiscusCore2
* appId (required) : your qiscus multichannel appid
* config (optional) : custom config for multichannel widget
* localPrefKey (required) : uniq local data can random
*/
val configMultichannel: MultichannelWidgetConfig =
        MultichannelWidgetConfig.setEnableLog(BuildConfig.DEBUG)
                .setNotificationListener(null)
                .setRoomTitle("Custom Room Title")
                .setRoomSubtitle("Custom subtitle")
        MultichannelWidget.setup(this, ConstCore.qiscusCore1(), "yourAppId", configMultichannel, "user1")
```

## Initiate chat

Start chatting can be done in single method, here is how you can do that

```
/**
* context (required) : context activity
* qiscusCore (required) : QiscusCore
* nama (required) : username
* userid (required) : userId from user
* avatar (optional) : user avatar
* extras (optional) : extra data (json)
* userProperties (optional) : user properties for multichannel (Map)
*/
MultichannelWidget.instance.initiateChat(context, qiscusCore,name, userId, avatar, extras, userProperties)
```

for example  :
```
//login user1
MultichannelWidget.instance.initiateChat(this, ConstCore.qiscusCore1(), "yourName", "yourUserId1","yourAvatar1", null, userProperties1)
```

## Push Notification

first you need setup Firebase Cloud Messaging in your android App. You need register your server key to Qiscus Multichannel. For now, we can help you to add this to multichannel, just create ticket to Qiscus [support](https://support.qiscus.com/hc/en-us/requests/new) and send your server key and app id.

In your app, you need to register FCM token to notify Qiscus Multichannel, and call getCurrentDeviceToken() after initiateChat, and in 1 life circle. For example
```
class FirebaseServices : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        MultichannelWidget.instance.registerDeviceToken(ConstCore.qiscusCore1(), p0)
    }

    fun getCurrentDeviceToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(
                        "Qiscus", "getCurrentDeviceToken Failed : " +
                                task.exception
                    )
                    return@OnCompleteListener
                }
                if (task.result != null) {
                    val currentToken = task.result!!.token
                    MultichannelWidget.instance.registerDeviceToken(ConstCore.qiscusCore1(), currentToken)
                }
            })
     }
}
```

```
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //always call when aktif app
        if (MultichannelWidget.instance.hasSetupUser() == true) {
            FirebaseServices().getCurrentDeviceToken()
        }


        val userProperties = mapOf("city" to "jogja", "job" to "developer")

        btnOpen.setOnClickListener {
            MultichannelWidget.instance.initiateChat(this, "user1", "user@test.net","https://vignette.wikia.nocookie.net/fatal-fiction-fanon/images/9/9f/Doraemon.png/revision/latest?cb=20170922055255", null, userProperties)

            // only 1 after initiateChat
            if (MultichannelWidget.instance.hasSetupUser() == true) {
                FirebaseServices().getCurrentDeviceToken()
            }
        }
    }
```

to handle incoming message from Qiscus Multichannel you can do this

```
 override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        if (MultichannelWidget.instance.isMultichannelMessage(p0, ConstCore.allQiscusCore())) {
            Log.e("debug", "notif")
            return
        }
 }
```

By default, Multichannel widget just show notification. If you need more than that or you want to custom your notification. You can do that in config before init

```
val configMultichannel: MultichannelWidgetConfig =
            MultichannelWidgetConfig.setEnableLog(BuildConfig.DEBUG)
                .setNotificationListener(object : MultichannelNotificationListener {
                    override fun handleMultichannelListener(
                        context: Context?,
                        qiscusComment: QMessage?
                    ) {
                        //do something here
                    }

                })
```

some configuration can set when call MultichannelWidgetConfig, for example

```
        val configMultichannel: MultichannelWidgetConfig =
            MultichannelWidgetConfig.setEnableLog(BuildConfig.DEBUG)
                 .setNotificationListener(object : MultichannelNotificationListener {
                                    override fun handleMultichannelListener(
                                        context: Context?,
                                        qiscusComment: QMessage?
                                    ) {
                                        //do something here
                                    }

                                })
                .setRoomTitle("Bot name") //title in navigationBar
                .setRoomSubtitle("Custom subtitle") //subtitle in navigationBar
                .setHideUIEvent(true) // hide UI Event in chat
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
