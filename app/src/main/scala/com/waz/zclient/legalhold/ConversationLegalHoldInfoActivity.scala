package com.waz.zclient.legalhold

import android.content.{Context, Intent}
import com.waz.model.UserId
import com.waz.zclient.R
import com.waz.zclient.conversation.ConversationController
import com.wire.signals.Signal

class ConversationLegalHoldInfoActivity extends BaseLegalHoldInfoActivity {

  override lazy val legalHoldUsers: Signal[Seq[UserId]] =
    for {
      convId <- inject[ConversationController].currentConvId
      users  <- inject[LegalHoldController].legalHoldUsers(convId)
    } yield users

  override lazy val legalHoldInfoMessage: Int = R.string.legal_hold_conversation_info_message

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
