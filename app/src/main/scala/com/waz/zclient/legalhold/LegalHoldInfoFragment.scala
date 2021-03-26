package com.waz.zclient.legalhold

import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import androidx.recyclerview.widget.{LinearLayoutManager, RecyclerView}
import com.waz.utils.returning
import com.waz.zclient.pages.BaseFragment
import com.waz.zclient.ui.text.TypefaceTextView
import com.waz.zclient.{FragmentHelper, R}

class LegalHoldInfoFragment extends BaseFragment[LegalHoldInfoFragment.Container]()
  with FragmentHelper {

  import LegalHoldInfoFragment._

  private lazy val infoMessageTextView = view[TypefaceTextView](R.id.legal_hold_info_message_text_view)
  private lazy val subjectsRecyclerView = view[RecyclerView](R.id.legal_hold_info_subjects_recycler_view)

  private lazy val adapter = returning(new LegalHoldUsersAdapter(users, Some(MAX_PARTICIPANTS))) { a =>
    setAdapterClickListeners(a)
  }

  private lazy val users = getContainer.legalHoldUsers.map(_.toSet)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View =
    inflater.inflate(R.layout.fragment_legal_hold_info, container, false)

  override def onViewCreated(view: View, savedInstanceState: Bundle): Unit = {
    super.onViewCreated(view, savedInstanceState)
    setMessage()
    setUpRecyclerView()
  }

  private def setMessage(): Unit =
      infoMessageTextView.foreach(_.setText(getContainer.legalHoldInfoMessage))

  private def setUpRecyclerView(): Unit = {
    subjectsRecyclerView.foreach { recyclerView =>
      recyclerView.setLayoutManager(new LinearLayoutManager(getContext))
      recyclerView.setAdapter(adapter)
    }
  }

  private def setAdapterClickListeners(adapter: LegalHoldUsersAdapter): Unit = {
    adapter.onClick(getContainer.onLegalHoldUserClick)
    adapter.onShowAllParticipantsClick { _ => getContainer.onShowAllLegalHoldUsersClick() }
  }
}

object LegalHoldInfoFragment {

  trait Container extends LegalHoldListContainer {
    val legalHoldInfoMessage: Int

    def onShowAllLegalHoldUsersClick(): Unit = {}
  }

  private val MAX_PARTICIPANTS = 4

  def newInstance() = new LegalHoldInfoFragment()
}
