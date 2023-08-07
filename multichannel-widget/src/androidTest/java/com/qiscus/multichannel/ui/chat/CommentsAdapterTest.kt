package com.qiscus.multichannel.ui.chat

import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.ui.chat.viewholder.AudioVH
import com.qiscus.multichannel.ui.chat.viewholder.BaseViewHolder
import com.qiscus.multichannel.ui.chat.viewholder.TextVH
import com.qiscus.multichannel.util.AudioHandler
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore
import com.qiscus.sdk.chat.core.data.model.QAccount
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QUser
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.util.QiscusConst
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.util.*

@ExtendWith(InstrumentationBaseTest::class)
internal class CommentsAdapterTest : InstrumentationBaseTest() {

    private lateinit var parentView: LinearLayout
    private var adapter: CommentsAdapter? = null
    private lateinit var config: QiscusMultichannelWidgetConfig
    private lateinit var handler: AudioHandler
    private lateinit var core: QiscusCore
    private lateinit var account: QAccount
    private val userMeId = "user@mail.com"
    private lateinit var dataStore: QiscusDataStore

    @BeforeAll
    fun setUp() {
        setUpComponent()
        MockitoAnnotations.openMocks(this)
        handler = mock()
        core = mock()
        account = mock()


        dataStore = mock()

        whenever(account.id).thenReturn(userMeId)
        whenever(core.qiscusAccount).thenReturn(account)
        whenever(core.apps).thenReturn(application!!)
        whenever(core.appsHandler).thenReturn(Handler(application!!.mainLooper))
        whenever(core.androidUtil).thenReturn(QiscusAndroidUtil(core))
        whenever(core.dataStore).thenReturn(dataStore)
        whenever(dataStore.getLocalPath(Mockito.anyLong())).thenReturn(File("path"))

        MultichannelConst.setQiscusCore(core)
        QiscusConst.setApps(application!!)

        config = QiscusMultichannelWidgetConfig()
        config.prepare(context!!)
        config.setAvatar(QiscusMultichannelWidgetConfig.Avatar.DISABLE)
        ResourceManager.setUp(context!!, QiscusMultichannelWidgetColor())

        parentView = LinearLayout(context)
        adapter = CommentsAdapter(
            context!!, mock(), QiscusMultichannelWidgetColor(), handler
        )
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun compareIdSameTest() {
        val message1 = createMessage(102, Date())
        val message2 = createMessage(102, Date())
        val position = compare(message1, message2)
        assertEquals(position, 0)
    }

    @Test
    fun compareIdMinusTest() {
        val message1 = createMessage(-1, Date(), "OKOKO")
        val message2 = createMessage(-1,
            Calendar.getInstance().apply {
                add(Calendar.DATE, -1)
            }.time, "msg", "x1111"
        )
        val position = compare(message1, message2)
        assertEquals(position, -1)
    }

    @Test
    fun compareIdNotMinusTest() {
        val message1 = createMessage(102, Date())
        val message2 = createMessage(103, Date())
        val position = compare(message1, message2)
        assertEquals(position, 1)
    }

    @Test
    fun compareIdSecondMinusTest() {
        val message1 = createMessage(102, Date())
        val message2 = createMessage(-1, Date())
        val position = compare(message1, message2)
        assertEquals(position, 1)
    }

    @Test
    fun compareIdFirstMinusTest() {
        val message1 = createMessage(-1, Date())
        val message2 = createMessage(102, Date())
        val position = compare(message1, message2)
        assertEquals(position, -1)
    }

    @Test
    fun compareTextNotSameTest() {
        val message1 = createMessage(201, Date(), "text_ok")
        val message2 = createMessage(-2, Date(), "msg", "xxx123")
        val position = compare(message1, message2)
        assertEquals(position, 0)
    }

    private fun createMessage(
        idMsg: Long, time: Date, message: String = "text", uniqueMsgId: String? = null, rawMsgType: String = "text"
    ) = QMessage().apply {
        id = idMsg
        chatRoomId = 100
        uniqueId = "unique_${uniqueMsgId ?: idMsg}"
        text = message
        rawType = rawMsgType
        timestamp = time
    }

    fun compare(message1: QMessage, message2: QMessage): Int {
        val compare = extractMethode(adapter!!, "compare")
        return compare.call(adapter!!, message1, message2) as Int
    }

    @Test
    fun getItemViewTypeTextMeTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            sender = QUser().apply {
                id = userMeId
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_TEXT, type)
    }

    @Test
    fun getItemViewTypeTextOpponentTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            sender = QUser().apply {
                id = userMeId + 1000
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_TEXT, type)
    }

    @Test
    fun getItemViewTypeFileMeTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            text = "[file] file.pdf [/file]"
            sender = QUser().apply {
                id = userMeId
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_FILE, type)
    }

    @Test
    fun getItemViewTypeFileOpponentTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            text = "[file] file.pdf [/file]"
            sender = QUser().apply {
                id = userMeId + 1000
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_FILE, type)
    }

    @Test
    fun getItemViewTypeAttachmentFileMeTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "file_attachment"
            text = "[file] file.pdf [/file]"
            sender = QUser().apply {
                id = userMeId
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_FILE, type)
    }

    @Test
    fun getItemViewTypeAttachmentFileOpponentTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "file_attachment"
            text = "[file] file.pdf [/file]"
            sender = QUser().apply {
                id = userMeId + 1000
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_FILE, type)
    }

    @Test
    fun getItemViewTypeAttachmentImageMeTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "file_attachment"
            text = "[file] file.png [/file]"
            sender = QUser().apply {
                id = userMeId
            }
            payload = "{ \"file_name\" : \"file.png\" }"
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_IMAGE, type)
    }

    @Test
    fun getItemViewTypeAttachmentImageOpponentTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "file_attachment"
            text = "[file] file.png [/file]"
            sender = QUser().apply {
                id = userMeId + 1000
            }
            payload = "{ \"file_name\" : \"file.png\" }"
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_IMAGE, type)
    }

    @Test
    fun getItemViewTypeAttachmentVideoMeTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "file_attachment"
            text = "[file] file.mp4 [/file]"
            sender = QUser().apply {
                id = userMeId
            }
            payload = "{ \"file_name\" : \"file.mp4\" }"
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_VIDEO, type)
    }

    @Test
    fun getItemViewTypeAttachmentVideoOpponentTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "file_attachment"
            text = "[file] file.mp4 [/file]"
            sender = QUser().apply {
                id = userMeId + 1000
            }
            payload = "{ \"file_name\" : \"file.mp4\" }"
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_VIDEO, type)
    }

    @Test
    fun getItemViewTypeAttachmentAudioOpponentTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "file_attachment"
            text = "[file] file.mp3 [/file]"
            sender = QUser().apply {
                id = userMeId + 1000
            }
            payload = "{ \"file_name\" : \"file.mp3\" }"
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_AUDIO, type)
    }

    @Test
    fun getItemViewTypeAttachmentAudioMeTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "file_attachment"
            text = "[file] file.mp3 [/file]"
            sender = QUser().apply {
                id = userMeId
            }
            payload = "{ \"file_name\" : \"file.mp3\" }"
        })

        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_AUDIO, type)
    }

    @Test
    fun getItemViewTypeCustomStickerMeTest() {
        val msg = adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "custom"
            text = "[sticker] https://www.example.com [/sticker]"
            sender = QUser().apply {
                id = userMeId
            }
        })

        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_STICKER, type)
    }

    @Test
    fun getItemViewTypeCustomStickerOpponentTest() {
        val msg = adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "custom"
            text = "[sticker] https://www.example.com [/sticker]"
            sender = QUser().apply {
                id = userMeId + 1000
            }
        })

        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_STICKER, type)
    }

    @Test
    fun getItemViewTypeLinkMeTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "text"
            text = "https://www.example.com"
            sender = QUser().apply {
                id = userMeId
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_TEXT, type)
    }

    @Test
    fun getItemViewTypeLinkOpponentTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "text"
            text = "https://www.example.com"
            sender = QUser().apply {
                id = userMeId + 1000
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_TEXT, type)
    }

    @Test
    fun getItemViewTypeStickerMeTest() {
        val msg =  createMessage(100, Date()).apply {
            rawType = "text"
            text = "https://www.example.com"
            sender = QUser().apply {
                id = userMeId
            }
        }

        val containsUrl = extractMethode(msg, "containsUrl").call(msg) as Boolean
        if (containsUrl) msg.text = "[sticker] https://www.example.com [/sticker]"

        adapter!!.data.add(msg)

        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_STICKER, type)
    }

    @Test
    fun getItemViewTypeStickerOpponentTest() {
        val msg = createMessage(100, Date()).apply {
            rawType = "text"
            text = "https://www.example.com"
            sender = QUser().apply {
                id = userMeId + 1000
            }
        }

        val containsUrl = extractMethode(msg, "containsUrl").call(msg) as Boolean
        if (containsUrl) msg.text = "[sticker] https://www.example.com [/sticker]"

        adapter!!.data.add(msg)

        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_STICKER, type)
    }

    @Test
    fun getItemViewTypeReplyMeTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "reply"
            text = "reply text"
            sender = QUser().apply {
                id = userMeId
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_REPLY, type)
    }

    @Test
    fun getItemViewTypeReplyOpponentTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "reply"
            text = "reply text"
            sender = QUser().apply {
                id = userMeId + 1000
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_REPLY, type)
    }

    @Test
    fun getItemViewTypeLocationMeTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "location"
            text = "location text"
            sender = QUser().apply {
                id = userMeId
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_MY_LOCATION, type)
    }

    @Test
    fun getItemViewTypeLocationOpponentTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "location"
            text = "location text"
            sender = QUser().apply {
                id = userMeId + 1000
            }
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_OPPONENT_LOCATION, type)
    }

    @Test
    fun getItemViewTypeSystemEventTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "system_event"
            text = "system_event text"
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_EVENT, type)
    }

    @Test
    fun getItemViewTypeCardTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "card"
            text = "card text"
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_CARD, type)
    }

    @Test
    fun getItemViewTypeCarouselTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "carousel"
            text = "carousel text"
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_CAROUSEL, type)
    }

    @Test
    fun getItemViewTypeButtonTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "buttons"
            text = "buttons text"
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_BUTTON, type)
    }

    @Test
    fun getItemViewTypeNotSupportTest() {
        adapter!!.data.add(
            createMessage(100, Date()).apply {
            rawType = "account_linking"
            text = "buttons text"
        })
        val type = adapter!!.getItemViewType(0)
        assertEquals(CommentsAdapter.TYPE_NOT_SUPPORT, type)
    }

    @Test
    fun onCreateViewHolderMyTextTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_MY_TEXT)
    }

    @Test
    fun onCreateViewHolderOpponentTextTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_OPPONENT_TEXT)
    }

    @Test
    fun onCreateViewHolderMyFileTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_MY_FILE)
    }

    @Test
    fun onCreateViewHolderOpponentFileTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_OPPONENT_FILE)
    }

    @Test
    fun onCreateViewHolderMyImageTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_MY_IMAGE)
    }

    @Test
    fun onCreateViewHolderOpponentImageTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_OPPONENT_IMAGE)
    }

    @Test
    fun onCreateViewHolderMyVideoTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_MY_VIDEO)
    }

    @Test
    fun onCreateViewHolderOpponentVideoTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_OPPONENT_VIDEO)
    }

    @Test
    fun onCreateViewHolderMyAudioTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_MY_AUDIO)
    }

    @Test
    fun onCreateViewHolderOpponentAudioTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_OPPONENT_AUDIO)
    }

    @Test
    fun onCreateViewHolderMyLocationTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_MY_LOCATION)
    }

    @Test
    fun onCreateViewHolderOpponentLocationTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_OPPONENT_LOCATION)
    }

    @Test
    fun onCreateViewHolderMyStickerTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_MY_STICKER)
    }

    @Test
    fun onCreateViewHolderOpponentStickerTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_OPPONENT_STICKER)
    }

    @Test
    fun onCreateViewHolderMyReplyTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_MY_REPLY)
    }

    @Test
    fun onCreateViewHolderOpponentReplyTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_OPPONENT_REPLY)
    }

    @Test
    fun onCreateViewHolderEventTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_EVENT)
    }

    @Test
    fun onCreateViewHolderCardTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_CARD)
    }

    @Test
    fun onCreateViewHolderCararouselTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_CAROUSEL)
    }

    @Test
    fun onCreateViewHolderButtonTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_BUTTON)
    }

    @Test
    fun onCreateViewHolderNotSupportTest() {
        val itemView = LinearLayout(context!!)
        adapter?.onCreateViewHolder(itemView, CommentsAdapter.TYPE_NOT_SUPPORT)
    }

    @Test
    fun getItemCountTest() {
        adapter?.itemCount
    }

    @Test
    fun onBindViewHolderSamePositionTest() {
        val itemView = getView(CommentsAdapter.TYPE_MY_AUDIO)
        val vh = AudioVH(
            itemView.rootView, config, QiscusMultichannelWidgetColor(), CommentsAdapter.TYPE_MY_AUDIO, getItemViewListener(), handler
        )
        val position = 0


        val msg = createMessage(100L, Date(), "text").apply {
            timestamp = Date()
        }
        adapter!!.data.add(msg)

        val msg2 = createMessage(100L, Date(), "text").apply {
            timestamp = Calendar.getInstance().apply {
                add(Calendar.DATE, -1)
            }.time
        }
        adapter!!.data.add(msg2)

        adapter!!.stopAnotherAudio(-2L)
        adapter?.onBindViewHolder(vh, position)
        adapter!!.data.remove(msg)
        adapter!!.data.remove(msg2)
    }

    @Test
    fun onBindViewHolderSamePositionPayerIdSameTest() {
        val itemView = getView(CommentsAdapter.TYPE_MY_AUDIO)
        val vh = AudioVH(
            itemView.rootView, config, QiscusMultichannelWidgetColor(), CommentsAdapter.TYPE_MY_AUDIO, getItemViewListener(), handler
        )
        val position = 0

        val msg = createMessage(100L, Date(), "text").apply {
            timestamp = Date()
        }
        adapter!!.data.add(msg)

        val msg2 = createMessage(100L, Date(), "text").apply {
            timestamp = Date()
        }
        adapter!!.data.add(msg2)

        adapter!!.stopAnotherAudio(100L)
        adapter?.onBindViewHolder(vh, position)
        adapter!!.data.remove(msg)
        adapter!!.data.remove(msg2)
    }

    @Test
    fun onBindViewHolderSamePositionPayerIdNotSameTest() {
        val itemView = getView(CommentsAdapter.TYPE_MY_AUDIO)
        val vh = AudioVH(
            itemView.rootView, config, QiscusMultichannelWidgetColor(), CommentsAdapter.TYPE_MY_AUDIO, getItemViewListener(), handler
        )
        val position = 0

        val msg = createMessage(100L, Date(), "text")
        adapter!!.data.add(msg)

        adapter!!.stopAnotherAudio(200L)
        adapter?.onBindViewHolder(vh, position)
        adapter!!.data.remove(msg)
    }

    @Test
    fun onBindViewHolderNotSamePositionTest() {
        val itemView = getView(CommentsAdapter.TYPE_MY_AUDIO)
        val vh = BaseViewHolder(
            itemView.rootView, config, QiscusMultichannelWidgetColor()
        )
        val position = 0

        val msg = createMessage(100L, Date(), "text").apply {
            timestamp = Date()
        }
        adapter!!.data.add(msg)

        val msg2 = createMessage(10112L, Date(), "textjhg").apply {
            timestamp = Calendar.getInstance().apply {
                add(Calendar.DATE, -1)
            }.time
        }
        adapter!!.data.add(msg2)

        adapter?.onBindViewHolder(vh, position)
        adapter!!.data.remove(msg)
        adapter!!.data.remove(msg2)
    }

    @Test
    fun isStickerTest() {
        val isSticker = extractMethode(adapter!!, "isSticker")
        isSticker.call(adapter!!, "[sticker] ok.gif [/sticker]")
    }

    @Test
    fun isStickerLastFalseTest() {
        val isSticker = extractMethode(adapter!!, "isSticker")
        isSticker.call(adapter!!, "[sticker] ok.gif [/img]")
    }

    @Test
    fun isStickeFirstFalserTest() {
        val isSticker = extractMethode(adapter!!, "isSticker")
        isSticker.call(adapter!!, "[img] ok.gif [/sticker]")
    }

    @Test
    fun isStickerNoExistTest() {
        val isSticker = extractMethode(adapter!!, "isSticker")
        isSticker.call(adapter!!, "ok.gif")
    }

    @Test
    fun onDetachedFromRecyclerViewTest() {
        adapter?.onDetachedFromRecyclerView(RecyclerView(context!!))
    }

    @Test
    fun setSelectedCommentTest() {
        adapter?.setSelectedComment(QMessage())
    }

    @Test
    fun getSelectedCommentTest() {
        adapter?.getSelectedComment()
    }

    private fun getItemViewListener() = object : CommentsAdapter.ItemViewListener {
        override fun onSendComment(comment: QMessage) {
        }

        override fun onItemClick(view: View, position: Int) {
        }

        override fun onItemLongClick(view: View, position: Int) {
        }

        override fun onItemReplyClick(view: View, comment: QMessage) {
        }

        override fun stopAnotherAudio(comment: QMessage) {
        }

    }

    @Test
    fun onViewRecycledAudioVHTest() {
        val itemView = getView(CommentsAdapter.TYPE_MY_AUDIO)

        adapter?.onViewRecycled(AudioVH(
            itemView.rootView, config, QiscusMultichannelWidgetColor(), CommentsAdapter.TYPE_MY_AUDIO, getItemViewListener(), handler
        ))
    }

    @Test
    fun onViewRecycledNotAudioVHTest() {
        val itemView = getView(CommentsAdapter.TYPE_MY_TEXT)

        adapter?.onViewRecycled(TextVH(
            itemView.rootView, config, QiscusMultichannelWidgetColor(), CommentsAdapter.TYPE_MY_TEXT
        ))
    }

    @Test
    fun getViewNotSupportTest() {
        getView(CommentsAdapter.TYPE_NOT_SUPPORT)
    }

    @Test
    fun getViewOpponentTextTest() {
        getView(CommentsAdapter.TYPE_OPPONENT_TEXT)
    }

    @Test
    fun getViewMyImageTest() {
        getView(CommentsAdapter.TYPE_MY_IMAGE)
    }

    @Test
    fun getViewOpponentImageTest() {
        getView(CommentsAdapter.TYPE_OPPONENT_IMAGE)
    }

    @Test
    fun getViewMyVideoTest() {
        getView(CommentsAdapter.TYPE_MY_VIDEO)
    }

    @Test
    fun getViewOpponentVideoTest() {
        getView(CommentsAdapter.TYPE_OPPONENT_VIDEO)
    }

    @Test
    fun getViewMyFileTest() {
        getView(CommentsAdapter.TYPE_MY_FILE)
    }
    @Test
    fun getViewOpponentFileTest() {
        getView(CommentsAdapter.TYPE_OPPONENT_FILE)
    }

    @Test
    fun getViewMyReplyTest() {
        getView(CommentsAdapter.TYPE_MY_REPLY)
    }
    @Test
    fun getViewOpponentReplyTest() {
        getView(CommentsAdapter.TYPE_OPPONENT_REPLY)
    }

    @Test
    fun getViewMyStickerTest() {
        getView(CommentsAdapter.TYPE_MY_STICKER)
    }

    @Test
    fun getViewOpponentStickerTest() {
        getView(CommentsAdapter.TYPE_OPPONENT_STICKER)
    }

    @Test
    fun getViewOpponentAudioTest() {
        getView(CommentsAdapter.TYPE_OPPONENT_AUDIO)
    }

    @Test
    fun getViewMyLocationTest() {
        getView(CommentsAdapter.TYPE_MY_LOCATION)
    }

    @Test
    fun getViewOpponentLocationTest() {
        getView(CommentsAdapter.TYPE_OPPONENT_LOCATION)
    }

    @Test
    fun getViewEventTest() {
        getView(CommentsAdapter.TYPE_EVENT)
    }

    @Test
    fun getViewCardTest() {
        getView(CommentsAdapter.TYPE_CARD)
    }

    @Test
    fun getViewCarouselTest() {
        getView(CommentsAdapter.TYPE_CAROUSEL)
    }

    @Test
    fun getViewButtonTest() {
        getView(CommentsAdapter.TYPE_BUTTON)
    }

    private fun getView(typeView: Int): View {
        val getView = extractMethode(adapter!!, "getView")
        return getView.call(adapter!!, parentView, typeView) as View
    }

    @Test
    fun removeTest() {
        adapter?.remove(QMessage())
    }

    @Test
    fun onInsertedTest() {
        val onInserted = extractMethode(adapter!!, "onInserted")
        onInserted.call(adapter!!, 0, 10)
    }

    @Test
    fun onRemovedTest() {
        val onRemoved = extractMethode(adapter!!, "onRemoved")
        onRemoved.call(adapter!!, 0, 10)
    }

    @Test
    fun onChangedTest() {
        val onChanged = extractMethode(adapter!!, "onChanged")
        onChanged.call(adapter!!, 0, 10)
    }

    @Test
    fun onMovedTest() {
        val onMoved = extractMethode(adapter!!, "onMoved")
        onMoved.call(adapter!!, 0, 10)
    }

    @Test
    fun addOrUpdateTest() {
        val list = arrayListOf<QMessage>()
        val msg = createMessage(201, Date(), "text_ok")
        list.add(msg)
        adapter!!.addOrUpdate(list)

        adapter!!.data.remove(msg)
    }

    @Test
    fun addOrUpdateSameTest() {
        val list = arrayListOf<QMessage>()
        val msg = createMessage(201, Date(), "text_ok")
        list.add(msg)
        adapter!!.data.add(msg)
        adapter!!.addOrUpdate(list)

        adapter!!.data.remove(msg)
    }

    @Test
    fun addOrUpdateSingleTest() {
        val msg = createMessage(201, Date(), "text_ok")
        adapter!!.addOrUpdate(msg)
        val msg2 = createMessage(201, Date(), "text_ok")
        adapter!!.addOrUpdate(msg2)

        adapter!!.data.remove(msg)
        adapter!!.data.remove(msg2)
    }

    @Test
    fun stopAnotherAudioTest() {
        val msg = createMessage(
            111L, Date(), "[file] file.mp3 [/file]", rawMsgType = "file_attachment"
        ).apply {
            payload = "{ \"file_name\" : \"file.mp3\" }"
        }
        adapter!!.data.add(msg)
        adapter!!.stopAnotherAudio(100L)
        adapter!!.data.remove(msg)
    }

    @Test
    fun stopAnotherAudioSameIdTest() {
        val msg = createMessage(
            100L, Date(), "[file] file.mp3 [/file]", rawMsgType = "file_attachment"
        ).apply {
            payload = "{ \"file_name\" : \"file.mp3\" }"
        }
        adapter!!.data.add(msg)
        adapter!!.stopAnotherAudio(100L)
        adapter!!.data.remove(msg)
    }

    @Test
    fun clearSelectedTest() {
        adapter!!.data.add(
            createMessage(
                100L, Date(), "text"
            ).apply {
                isSelected = true
            })
        adapter!!.data.add(
            createMessage(
                101L, Date(), "text"
            ).apply {
                isSelected = false
            })

        adapter!!.clearSelected()
        adapter!!.clear()
    }

    @Test
    fun clearSelectedByPositionTest() {
        adapter!!.data.add(
            createMessage(
                100L, Date(), "text"
            ).apply {
                isSelected = true
            })

        adapter!!.clearSelected(0)
        adapter!!.clearSelected(10)
        adapter!!.clear()
    }

    @Test
    fun getLatestSentCommentTest() {
        val msg =  createMessage(
            100L, Date(), "text"
        ).apply {
            status = QMessage.STATE_SENT
        }
        adapter!!.data.add(msg)
        val result = adapter!!.getLatestSentComment()
        assertEquals(msg, result)
        adapter!!.remove(msg)
    }

    @Test
    fun getLatestSentCommentStatusUnderSentTest() {
        val msg =  createMessage(
            100L, Date(), "text"
        ).apply {
            status = QMessage.STATE_SENDING
        }
        adapter!!.data.add(msg)
        val result = adapter!!.getLatestSentComment()
        assertNull(result)
        adapter!!.remove(msg)
    }

    @Test
    fun getLatestCommentTest() {
        val msg =  createMessage(
            100L, Date(), "text"
        ).apply {
            status = QMessage.STATE_SENDING
        }
        adapter!!.data.add(msg)
        val result = adapter!!.getLatestComment()
        assertEquals(msg, result)
        adapter!!.remove(msg)
    }

    @Test
    fun goToCommentTest() {
        val msg =  createMessage(100L, Date(), "text")
        adapter!!.data.add(msg)
        adapter!!.goToComment(msg.id) { it, _ ->
            adapter!!.remove(it)
            assertEquals(it, msg)
        }
    }

    @Test
    fun goToCommentIdNotValidTest() {
        val msg =  createMessage(100L, Date(), "text")
        adapter!!.data.add(msg)
        adapter!!.goToComment(1000L) { it, _ ->
            /* do nothings */
        }
        adapter!!.remove(msg)
    }

    @Test
    fun updateLastDeliveredCommentSameIdTest() {
        val msg = createMessage(100L, Date(), "text").apply {
            status = QMessage.STATE_DELIVERED
        }
        adapter!!.data.add(msg)

        adapter!!.updateLastDeliveredComment(100L)

        adapter!!.remove(msg)
    }

    @Test
    fun updateLastDeliveredCommentNotSameIdTest() {
        val msg = createMessage(100L, Date(), "text").apply {
            status = QMessage.STATE_DELIVERED
        }
        adapter!!.data.add(msg)

        adapter!!.updateLastDeliveredComment(101L)

        adapter!!.remove(msg)
    }

    @Test
    fun updateLastDeliveredCommentSendingStateTest() {
        val msg = createMessage(100L, Date(), "text").apply {
            status = QMessage.STATE_SENDING
        }
        adapter!!.data.add(msg)

        adapter!!.updateLastDeliveredComment(100L)

        adapter!!.remove(msg)
    }

    @Test
    fun updateLastReadCommentSameIdTest() {
        val msg = createMessage(100L, Date(), "text").apply {
            status = QMessage.STATE_READ
        }
        adapter!!.data.add(msg)

        adapter!!.updateLastReadComment(100L)

        adapter!!.remove(msg)
    }
    @Test
    fun updateLastReadCommentNotSameIdTest() {
        val msg = createMessage(100L, Date(), "text").apply {
            status = QMessage.STATE_READ
        }
        adapter!!.data.add(msg)

        adapter!!.updateLastReadComment(101L)

        adapter!!.remove(msg)
    }

}