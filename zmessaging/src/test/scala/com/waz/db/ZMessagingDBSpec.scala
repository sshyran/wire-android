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
package com.waz.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.waz.api.Message
import com.waz.model.AssetData.AssetDataDao
import com.waz.model.AssetMetaData.Image.Tag.Medium
import com.waz.model.ConversationData.ConversationDataDao
import com.waz.model.MessageData.MessageDataDao
import com.waz.model.MsgDeletion.MsgDeletionDao
import com.waz.model.UserData.UserDataDao
import com.waz.model._
import com.waz.model.sync.SyncJob.SyncJobDao
import com.waz.utils.wrappers.{DB, URI}
import com.waz.utils.{DbLoader, returning}
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.threeten.bp.Instant

@RunWith(classOf[JUnitRunner])
@Ignore class ZMessagingDBSpec extends FeatureSpec with Matchers with Inspectors with GeneratorDrivenPropertyChecks with BeforeAndAfter with RobolectricTests with DbLoader {
  lazy val dbHelper = new ZMessagingDB(Robolectric.application, "test_db")

  after {
    dbHelper.close()
    Robolectric.application.getDatabasePath(dbHelper.getDatabaseName).delete()
  }

  feature("Database migrations") {

    scenario("Load db from binary file") {
      val db = loadDb("/db/zmessaging_60.db")
      val c = db.rawQuery("select * from Users")
      c should not be null
      c.getCount should be > 0
      db.close()
    }

    scenario("Drop all data for older db versions") {
      implicit val db = loadDb("/db/zmessaging_60.db")

      dbHelper.onUpgrade(db, 59, ZMessagingDB.DbVersion)
      UserDataDao.list should be(empty)
      MessageDataDao.list should be(empty)

      db.close()
    }

    scenario("Migrate UserData from 60") {
      implicit val db: DB = loadDb("/db/zmessaging_60.db")

      val numberOfUsersBeforeMigration = countUsers
      dbHelper.onUpgrade(db, 60, 90)
      countUsers shouldEqual numberOfUsersBeforeMigration
      UserDataDao.list should have size numberOfUsersBeforeMigration
      UserDataDao.list foreach { user =>
        user.relation should not be null
      }
    }

    scenario("Load AssetData from 60") {
      implicit val db = loadDb("/db/zmessaging_60.db")

      dbHelper.onUpgrade(db, 60, ZMessagingDB.DbVersion)
      val assets = AssetDataDao.list
      assets should not be empty
      assets.map(_.id).toSet should have size assets.size
    }

    scenario("Load ImageAssetData from 60") {
      implicit var db = loadDb("/db/zmessaging_60.db")
      dbHelper.onUpgrade(db, 60, ZMessagingDB.DbVersion)

      // XXX: db cursors keep some cache for column indexes, and this causes errors when reusing tables
      // hopefully this is only a problem in robolectric db driver
      // reopening the db fixes the problem
      val path = db.getPath
      db.close()

      val assets = AssetDataDao.list
      assets should have size 57

      val data = AssetData(metaData = Some(AssetMetaData.Image(Dim2(12, 13), Medium)), source = Some(URI.parse("url")))
      val im = AssetDataDao.insertOrReplace(data)
      im shouldEqual data
      AssetDataDao.list should have size 58
      AssetDataDao.getById(data.id) shouldEqual Some(data)
    }


    scenario("Load MessageData from 60") {
      implicit val db = loadDb("/db/zmessaging_60.db")

      dbHelper.onUpgrade(db, 60, ZMessagingDB.DbVersion)
      val data = MessageDataDao.list
      data should have size 994
    }

    scenario("Load ConversationData from 60") {
      implicit val db = loadDb("/db/zmessaging_60.db")

      dbHelper.onUpgrade(db, 60, ZMessagingDB.DbVersion)
      val convs = ConversationDataDao.list
      convs should have size 72
      convs foreach { conv =>
        conv.missedCallMessage shouldEqual None
        conv.incomingKnockMessage shouldEqual None
        conv.lastRead should be >= RemoteInstant.Epoch
      }
    }

    scenario("MsgDeletion table added in 68") {
      implicit val db = loadDb("/db/zmessaging_60.db")
      dbHelper.onUpgrade(db, 60, 68)

      val entry = MsgDeletion(MessageId(), Instant.now())
      MsgDeletionDao.insertOrReplace(entry)
      MsgDeletionDao.list should contain only entry
    }

    scenario("Migrate to protobuf model in 69") {
      implicit val db = loadDb("/db/zmessaging_60.db")
      dbHelper.onUpgrade(db, 60, ZMessagingDB.DbVersion)

      val msgs = MessageDataDao.list
      msgs should have size 994
      msgs foreach { m =>
        if (m.msgType == Message.Type.KNOCK) m.genericMsgs should have size 1
      }
    }

    scenario("Add message editTime column in 71") {
      implicit val db = loadDb("/db/zmessaging_60.db")
      dbHelper.onUpgrade(db, 60, ZMessagingDB.DbVersion)

      val msgs = MessageDataDao.list
      msgs should have size 994
      msgs foreach { m =>
        if (m.msgType == Message.Type.KNOCK) m.genericMsgs should have size 1
        m.editTime shouldEqual Instant.EPOCH
      }
    }

    scenario("Drop excludeFromPYMK and search from sync jobs in 75") {
      implicit val db = loadDb("/db/zmessaging_60.db")

      val before = SyncJobDao.list

      before should have size 100

      db.insert(SyncJobDao.table.name, SQLiteDatabase.CONFLICT_REPLACE, returning(new ContentValues) { cv =>
        cv.put("_id", "f94af0bf-4043-4278-8891-6d5562c266b3")
        cv.put("data",
          """{
            |  "id": "f94af0bf-4043-4278-8891-6d5562c266b3",
            |  "request": {
            |    "cmd": "sync-search",
            |    "queryCacheKey": 42
            |  },
            |  "priority": 10,
            |  "timestamp": 1471360327536,
            |  "startTime": 0,
            |  "state": "WAITING"
            |}
          """.stripMargin)
      })

      db.insert(SyncJobDao.table.name, SQLiteDatabase.CONFLICT_REPLACE, returning(new ContentValues) { cv =>
        cv.put("_id", "7c75141c-29f3-47d1-b52d-715991a81fdf")
        cv.put("data",
          """{
            |  "id": "7c75141c-29f3-47d1-b52d-715991a81fdf",
            |  "request": {
            |    "cmd": "post-exclude-pymk",
            |    "user": "33c897d1-276a-4c1b-a3bf-9c51c374e661"
            |  },
            |  "priority": 10,
            |  "timestamp": 1471360406264,
            |  "startTime": 0,
            |  "state": "WAITING"
            |}
          """.stripMargin)
      })

      dbHelper.onUpgrade(db, 60, 75)

      SyncJobDao.list should have size 100
      SyncJobDao.list shouldEqual before
    }

    scenario("Remove unused columns from Conversations in 90") {
      implicit val db = loadDb("/db/zmessaging_60.db")
      dbHelper.onUpgrade(db, 60, ZMessagingDB.DbVersion)
      val convs = ConversationDataDao.list
      convs should have size 72
      convs.collect { case c if !c.isActive => c } should have size 1
    }

  }


  def countUsers(implicit db: DB) = db.query("SELECT * FROM Users").getCount.toLong
}
