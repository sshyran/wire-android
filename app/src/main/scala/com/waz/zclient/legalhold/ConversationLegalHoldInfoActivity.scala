package com.waz.zclient.legalhold

import android.content.{Context, Intent}
import androidx.fragment.app.Fragment
import com.waz.model.UserId
import com.waz.zclient.R
import com.waz.zclient.conversation.ConversationController
import com.waz.zclient.participants.ParticipantsController
import com.waz.zclient.participants.fragments.SingleParticipantFragment
import com.wire.signals.Signal

class ConversationLegalHoldInfoActivity extends BaseLegalHoldInfoActivity {

  private lazy val participantsController = inject[ParticipantsController]

  override lazy val legalHoldUsers: Signal[Seq[UserId]] =
    for {
      convId <- inject[ConversationController].currentConvId
      users  <- inject[LegalHoldController].legalHoldUsers(convId)
    } yield users

  override lazy val legalHoldInfoMessage: Int = R.string.legal_hold_conversation_info_message

  private def showNextFragment(fragment: Fragment, tag: Option[String]): Unit = {
    val transaction = getSupportFragmentManager.beginTransaction()
      .setCustomAnimations(
        R.anim.fragment_animation_second_page_slide_in_from_right,
        R.anim.fragment_animation_second_page_slide_out_to_left,
        R.anim.fragment_animation_second_page_slide_in_from_left,
        R.anim.fragment_animation_second_page_slide_out_to_right)
      .replace(R.id.legal_hold_info_fragment_container_layout, fragment)

    tag.foreach(transaction.addToBackStack)

    transaction.commit()
  }

  override def onLegalHoldUserClick(userId: UserId): Unit = {
    import SingleParticipantFragment._
    participantsController.selectedParticipant ! Some(userId)
    showNextFragment(SingleParticipantFragment.newInstance(Some(DevicesTab.str), showFooter = false), Some(Tag))
  }

  override def onShowAllLegalHoldUsersClick(): Unit = {
    //TODO show a list of all legal hold subjects
  }
}

object ConversationLegalHoldInfoActivity {
  def newIntent(context: Context) = new Intent(context, classOf[ConversationLegalHoldInfoActivity])
}
