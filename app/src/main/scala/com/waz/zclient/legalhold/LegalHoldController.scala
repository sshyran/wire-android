package com.waz.zclient.legalhold

import com.waz.model.{ConvId, UserId}
import com.wire.signals.Signal

//TODO: implement status calculation
class LegalHoldController {

  def isLegalHoldActive(userId: UserId): Signal[Boolean] =
    Signal.const(true)

  def isLegalHoldActive(conversationId: ConvId): Signal[Boolean] =
    Signal.const(true)

  def legalHoldUsers(conversationId: ConvId): Signal[Seq[UserId]] =
    Signal.const(Seq(
      UserId("67c17307-5a27-4744-be41-6aacf19b50df"),
      UserId("9502b925-30f3-40e3-8ebc-4e148d6ef70a"),
      UserId("6d29085a-4d70-4e82-9d5e-27346362da8f"),
      UserId("586e9cc0-86d4-4a49-80c8-8dc2024ff24f"),
      UserId("a5b10681-1753-4d7b-b2c3-42eccf617cf1"),
      UserId("f4506d17-2b35-4521-8d2f-d76028d0bf5c"),
      UserId("e3691a2f-014b-40b3-bec8-725939292fe0"),
      UserId("5ea90572-ef60-4371-ad47-f8d8e14a7b5f"),
      UserId("2b8e5e5c-2c59-419c-89c4-07bf691d4941"),
      UserId("6c5a3f8d-0bc3-4d63-835b-3b2379fba7d8"),
      UserId("276209c9-2024-4916-a5b6-bfd65b1ca641")
    ))
}
