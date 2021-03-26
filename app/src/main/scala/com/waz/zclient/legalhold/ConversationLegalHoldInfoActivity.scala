package com.waz.zclient.legalhold

import android.app.Activity
import android.content.{Context, Intent}
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.waz.model.UserId
import com.waz.threading.Threading
import com.waz.zclient.{BaseActivity, R}
import com.waz.zclient.common.views.GlyphButton
import com.waz.zclient.conversation.ConversationController
import com.waz.zclient.participants.ParticipantsController
import com.waz.zclient.participants.fragments.SingleParticipantFragment
import com.waz.zclient.utils.RichView
import com.wire.signals.Signal

class ConversationLegalHoldInfoActivity extends BaseActivity with LegalHoldInfoFragment.Container {

  import ConversationLegalHoldInfoActivity._
  import Threading.Implicits.Ui

  private lazy val participantsController = inject[ParticipantsController]

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

  private def setUpCloseButton(): Unit = closeButton.onClick {
    setResult(Activity.RESULT_OK)
    finish()
  }

  private def showLegalHoldInfo(): Unit = {
    getSupportFragmentManager.beginTransaction()
      .add(R.id.legal_hold_info_fragment_container_layout, LegalHoldInfoFragment.newInstance())
      .commit()
  }

  private def showFragment(fragment: Fragment, tag: Option[String]): Unit = {
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
    participantsController.isGroup.head.foreach {
      case true  => openNewUserScreenForDeviceList(userId)
      case false => reOpenUserScreenForDeviceList()
    }
  }

  private def openNewUserScreenForDeviceList(userId: UserId): Unit = {
    import SingleParticipantFragment._
    participantsController.selectedParticipant ! Some(userId)
    showFragment(SingleParticipantFragment.newInstance(Some(DevicesTab.str), showFooter = false), Some(Tag))
  }

  private def reOpenUserScreenForDeviceList(): Unit = {
    setResult(RESULT_CODE_SHOW_USER_DEVICES)
    finish()
  }

  override def onShowAllLegalHoldUsersClick(): Unit = {
    //TODO show a list of all legal hold subjects
  }
}

object ConversationLegalHoldInfoActivity {
  val RESULT_CODE_SHOW_USER_DEVICES = 78932

  def newIntent(context: Context) = new Intent(context, classOf[ConversationLegalHoldInfoActivity])
}
