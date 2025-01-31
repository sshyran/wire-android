/*
 * Wire
 * Copyright (C) 2016 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz

import android.database.Cursor
import android.os.Parcel
import com.waz.cache.CacheEntryData
import com.waz.content.MsgCursor
import com.waz.model.{otr => _, _}
import com.waz.service.messages.MessageAndLikes
import com.waz.threading.Threading
import com.wire.signals.{EventContext, FlatMapSignal, Signal, Subscription}
import com.waz.utils.{CachedStorageImpl, Cleanup, Managed, returning}
import libcore.net.MimeUtils
import org.scalactic.Equality
import org.scalatest.enablers.Emptiness

import scala.collection.JavaConverters._
import scala.language.implicitConversions
import scala.util.Random

package object testutils {

  private implicit lazy val ec = Threading.Background

  object Implicits {

    implicit lazy val CursorEmptiness: Emptiness[Cursor] = new Emptiness[Cursor] {
      override def isEmpty(thing: Cursor): Boolean = thing.getCount == 0
    }

    implicit lazy val UiSignalEmptiness: Emptiness[api.UiSignal[_]] = new Emptiness[api.UiSignal[_]] {
      override def isEmpty(thing: api.UiSignal[_]): Boolean = thing.isEmpty
    }

    implicit class MessagesCursorSeq(list: MsgCursor) extends Seq[MessageAndLikes] {
      override def length: Int = list.size
      override def apply(idx: Int): MessageAndLikes = list(idx)
      override def iterator: Iterator[MessageAndLikes] = new Iterator[MessageAndLikes]() {
        var idx = 0
        override def hasNext: Boolean = idx < list.size
        override def next(): MessageAndLikes = {
          idx += 1
          list(idx - 1)
        }
      }
    }

    implicit class EnrichedInt(val a: Int) extends AnyVal {
      def times(f: => Unit): Unit = (1 to a) foreach (_ => f)
    }

    implicit class UiSignalIsSignal[A](val s: api.UiSignal[A]) extends AnyVal {
      def signal : Signal[A] = new Signal[A] with api.Subscriber[A] {
        var sub = Option.empty[api.Subscription]

        override def next(value: A): Unit = set(Some(value), Some(Threading.Ui))

        override def onWire(): Unit = sub = Some(s.subscribe(this))

        override def onUnwire(): Unit = {
          sub.foreach(_.cancel())
          sub = None
        }
      }
    }

    implicit class UiObservableCanBeSignal[A <: api.UiObservable](val a: A) extends AnyVal {
      def signal[B](f: A => B): Signal[B] = new Signal[B] with api.UpdateListener {

        override def updated(): Unit = set(Some(f(a)), Some(Threading.Ui))

        override def onWire(): Unit = {
          a.addUpdateListener(this)
          updated()
        }

        override def onUnwire(): Unit = a.removeUpdateListener(this)
      }

      def flatSignal[B](f: A => Signal[B]): Signal[B] = signal(identity).flatMap(f)
    }

    implicit class SignalToSink[A](val signal: Signal[A]) extends AnyVal {
      def sink: SignalSink[A] = returning(new SignalSink[A])(_.subscribe(signal))
    }

    class SignalSink[A] {
      @volatile private var sub = Option.empty[Subscription]
      def subscribe(s: Signal[A])(implicit ctx: EventContext = EventContext.Global): Unit = sub = Some(s(v => value = Some(v)))
      def unsubscribe: Unit = sub.foreach { s =>
        s.destroy()
        sub = None
      }
      @volatile private[testutils] var value = Option.empty[A]
      def current: Option[A] = value
    }

    implicit object GenericMessageEquality extends Equality[GenericMessage] {
      override def areEqual(a: GenericMessage, b: Any): Boolean = {
        b match {
          case m: GenericMessage => m.proto.toByteArray.sameElements(a.proto.toByteArray)
          case _ => false
        }
      }
    }

    implicit object MessageDataEquality extends Equality[MessageData] {
      override def areEqual(a: MessageData, b: Any): Boolean = {
        b match {
          case m: MessageData =>
            if (m.copy(genericMsgs = Nil) != a.copy(genericMsgs = Nil)) println(s"message content differ: \n$a\n$m\n")
            m.copy(genericMsgs = Nil) == a.copy(genericMsgs = Nil) && m.genericMsgs.size == a.genericMsgs.size && m.genericMsgs.zip(a.genericMsgs).forall { case (p1, p2) => GenericMessageEquality.areEqual(p1, p2) }
          case _ => false
        }
      }
    }
  }

  lazy val knownMimeTypes = returning(classOf[MimeUtils].getDeclaredField("mimeTypeToExtensionMap"))(_.setAccessible(true)).get(null).asInstanceOf[java.util.Map[String, String]].asScala.keys.toVector.map(Mime(_))

  def withParcel[A](f: Parcel => A): A = Managed(Parcel.obtain).acquire(f)
  implicit lazy val ParcelCleanup: Cleanup[Parcel] = new Cleanup[Parcel] { def apply(a: Parcel): Unit = a.recycle() }

  implicit object CacheEntryEquality extends Equality[CacheEntryData] {
    override def areEqual(a: CacheEntryData, b: Any): Boolean = {
      b match {
        case CacheEntryData(k, d, lu, t, p, e, f, m, i, l) =>
          k == a.key && lu == a.lastUsed && t == a.timeout && p == a.path && i == a.fileId && d.map(_.toSeq) == a.data.map(_.toSeq) && m == a.mimeType && f == a.fileName && l == a.length
        case _ => false
      }
    }
  }

  implicit object CacheEntryOptionEquality extends Equality[Option[CacheEntryData]] {
    override def areEqual(a: Option[CacheEntryData], b: Any): Boolean = (a, b) match {
      case (None, None) => true
      case (Some(entry), Some(b)) => CacheEntryEquality.areEqual(entry, b)
      case _ => false
    }
  }


  implicit class RichStorage[K, V <: com.waz.utils.Identifiable[K]](storage: CachedStorageImpl[K, V]) {
    def deleteAll() = storage.list().flatMap { vs => storage.removeAll(vs.map(_.id)) }
  }

  def randomPhoneNumber = PhoneNumber("+0" + (Random.nextInt(9) + 1).toString + Array.fill(13)(Random.nextInt(10)).mkString)
}
