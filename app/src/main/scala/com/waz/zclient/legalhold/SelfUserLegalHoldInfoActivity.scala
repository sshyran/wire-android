package com.waz.zclient.legalhold

import android.content.{Context, Intent}
import com.waz.model.UserId
import com.waz.zclient.R
import com.waz.zclient.messages.UsersController
import com.wire.signals.Signal

class SelfUserLegalHoldInfoActivity extends BaseLegalHoldInfoActivity {

  override lazy val legalHoldUsers: Signal[Seq[UserId]] =
    inject[UsersController].selfUser.map(user => Seq(user.id))

  override lazy val legalHoldInfoMessage: Int = R.string.legal_hold_self_user_info_message

  override def finish(): Unit = {
    super.finish()
    overridePendingTransition(R.anim.fade_in, R.anim.out_to_bottom_pop_exit)
  }
}

object SelfUserLegalHoldInfoActivity {
  def newIntent(context: Context) = new Intent(context, classOf[SelfUserLegalHoldInfoActivity])
}
