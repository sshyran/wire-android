package com.waz.zclient.legalhold

import android.content.{Context, Intent}
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.waz.model.UserId
import com.waz.zclient.{BaseActivity, R}
import com.waz.zclient.common.views.GlyphButton
import com.waz.zclient.conversation.ConversationController
import com.waz.zclient.utils.RichView
import com.wire.signals.Signal

class ConversationLegalHoldInfoActivity extends BaseActivity with LegalHoldInfoFragment.Container {

  private lazy val closeButton = findById[GlyphButton](R.id.legal_hold_info_close_button)

  override lazy val legalHoldUsers: Signal[Seq[UserId]] =
    for {
      convId <- inject[ConversationController].currentConvId
      users  <- inject[LegalHoldController].legalHoldUsers(convId)
    } yield users

  override lazy val legalHoldInfoMessage: Int = R.string.legal_hold_conversation_info_message

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_legal_hold_info)
    setUpCloseButton()
    showLegalHoldInfo()
  }

  private def setUpCloseButton(): Unit = closeButton.onClick { finish() }

  private def showLegalHoldInfo(): Unit = {
    showFragment(LegalHoldInfoFragment.newInstance(), None)
  }

  private def showFragment(fragment: Fragment, tag: Option[String]): Unit = {
    val transaction = getSupportFragmentManager.beginTransaction()
      .replace(R.id.legal_hold_info_fragment_container_layout, fragment)
    tag.foreach(transaction.addToBackStack)
    transaction.commit()
  }

  override def onLegalHoldUserClick(userId: UserId): Unit = {
    //TODO: show devices of user
  }

  override def onShowAllLegalHoldUsersClick(): Unit = {
    //TODO show a list of all legal hold subjects
  }
}

object ConversationLegalHoldInfoActivity {
  def newIntent(context: Context) = new Intent(context, classOf[ConversationLegalHoldInfoActivity])
}
