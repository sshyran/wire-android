package com.waz.zclient.legalhold

import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import androidx.recyclerview.widget.{LinearLayoutManager, RecyclerView}
import com.waz.model.UserId
import com.waz.utils.returning
import com.waz.zclient.{FragmentHelper, R}
import com.waz.zclient.common.controllers.ThemeController
import com.waz.zclient.common.views.PickableElement
import com.waz.zclient.pages.BaseFragment
import com.waz.zclient.usersearch.views.{PickerSpannableEditText, SearchEditText}
import com.wire.signals.Signal

class AllLegalHoldSubjectsFragment extends BaseFragment[LegalHoldListContainer] with FragmentHelper {

  private lazy val users = getContainer.legalHoldUsers.map(_.toSet)

  private lazy val adapter = returning(new LegalHoldUsersAdapter(users, None)) {
    _.onClick(getContainer.onLegalHoldUserClick)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) =
    inflater.inflate(R.layout.all_participants_fragment, container, false)

  override def onViewCreated(view: View, savedInstanceState: Bundle): Unit = {
    initRecyclerView()
    initSearchBox()
  }

  private def initRecyclerView(): Unit =
    returning(findById[RecyclerView](R.id.recycler_view)) { recyclerView =>
      recyclerView.setLayoutManager(new LinearLayoutManager(getContext))
      recyclerView.setAdapter(adapter)
    }

  private def initSearchBox(): Unit =
    returning(findById[SearchEditText](R.id.search_box)) { searchBox =>
      searchBox.applyDarkTheme(inject[ThemeController].isDarkTheme)
      searchBox.setCallback(new PickerSpannableEditText.Callback {
        override def onRemovedTokenSpan(element: PickableElement): Unit = {}

        override def afterTextChanged(s: String): Unit = {
          adapter.filter ! s
        }
      })
    }
}

trait LegalHoldListContainer {
  val legalHoldUsers: Signal[Seq[UserId]]

  def onLegalHoldUserClick(userId: UserId): Unit = {}
}

object AllLegalHoldSubjectsFragment {
  val TAG = "AllLegalHoldSubjectsFragment"

  def newInstance() = new AllLegalHoldSubjectsFragment()
}
