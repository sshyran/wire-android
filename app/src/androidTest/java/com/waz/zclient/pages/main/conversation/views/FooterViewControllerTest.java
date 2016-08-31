/**
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.pages.main.conversation.views;

import android.support.test.runner.AndroidJUnit4;
import com.waz.api.IConversation;
import com.waz.api.Message;
import com.waz.api.User;
import com.waz.zclient.MainTestActivity;
import com.waz.zclient.R;
import com.waz.zclient.pages.main.conversation.views.row.footer.FooterViewController;
import com.waz.zclient.pages.main.conversation.views.row.footer.FooterViewControllerFactory;
import com.waz.zclient.pages.main.conversation.views.row.message.MessageAndSeparatorViewController;
import com.waz.zclient.pages.main.conversation.views.row.message.MessageViewController;
import com.waz.zclient.pages.main.conversation.views.row.message.MessageViewControllerFactory;
import com.waz.zclient.pages.main.conversation.views.row.separator.Separator;
import com.waz.zclient.testutils.ViewTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.bp.Instant;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.waz.zclient.testutils.CustomViewAssertions.hasText;
import static com.waz.zclient.testutils.CustomViewAssertions.isGone;
import static com.waz.zclient.testutils.CustomViewAssertions.isInvisible;
import static com.waz.zclient.testutils.CustomViewAssertions.isVisible;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class FooterViewControllerTest extends ViewTest<MainTestActivity> {

    public FooterViewControllerTest() {
        super(MainTestActivity.class);
    }

    @Test
    public void verifyICanLikeMessage() throws InterruptedException {
        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, false);
        when(message.isLikedByThisUser()).thenReturn(false);
        when(message.isLiked()).thenReturn(false);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.gtv__footer__like__button)).check(isGone());

        onView(withId(R.id.ltv__row_conversation__message)).check(isVisible());
        onView(withId(R.id.ltv__row_conversation__message)).perform(click());

        Thread.sleep(400);

        onView(withId(R.id.gtv__footer__like__button)).check(isVisible());
        onView(withId(R.id.gtv__footer__like__button)).perform(click());

        verify(message).like();
    }

    @Test
    public void verifyICanLikeMessageThatIsLikedByOthers() {
        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, false);
        when(message.isLikedByThisUser()).thenReturn(false);
        when(message.isLiked()).thenReturn(true);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.gtv__footer__like__button)).check(isVisible());
        onView(withId(R.id.gtv__footer__like__button)).perform(click());

        verify(message).like();
    }

    @Test
    public void verifyICanUnlikeMessage() throws InterruptedException {
        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, false);
        when(message.isLikedByThisUser()).thenReturn(true);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.gtv__footer__like__button)).check(isGone());

        onView(withId(R.id.ltv__row_conversation__message)).check(isVisible());
        onView(withId(R.id.ltv__row_conversation__message)).perform(click());

        Thread.sleep(400);

        onView(withId(R.id.gtv__footer__like__button)).check(isVisible());
        onView(withId(R.id.gtv__footer__like__button)).perform(click());

        verify(message).unlike();
    }

    @Test
    public void verifyICannotLikeSystemMessage() {
        MessageViewsContainer messageViewsContainer = ViewControllerMockHelper.getMockMessageViewsContainer(activity);

        Message memberLeaveMessage = createMockMessage(Message.Type.MEMBER_LEAVE, Message.Status.SENT, false);
        FooterViewController memberLeaveFooterViewController = FooterViewControllerFactory.create(activity, memberLeaveMessage, messageViewsContainer);
        assertTrue(memberLeaveFooterViewController  == null);

        Message memberJoinMessage = createMockMessage(Message.Type.MEMBER_JOIN, Message.Status.SENT, false);
        FooterViewController memberJoinFooterViewController = FooterViewControllerFactory.create(activity, memberJoinMessage, messageViewsContainer);
        assertTrue(memberJoinFooterViewController  == null);

        Message connectAcceptedMessage = createMockMessage(Message.Type.CONNECT_ACCEPTED, Message.Status.SENT, false);
        FooterViewController connectAcceptedFooterViewController = FooterViewControllerFactory.create(activity, connectAcceptedMessage, messageViewsContainer);
        assertTrue(connectAcceptedFooterViewController == null);
    }

    @Test
    public void verifyICanSeeIfSomeoneElseLikedMessage() {
        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, false);
        when(message.isLikedByThisUser()).thenReturn(false);
        when(message.isLiked()).thenReturn(true);
        User likedUser = mock(User.class);
        when(likedUser.getId()).thenReturn("456");
        when(likedUser.getDisplayName()).thenReturn("Barry");
        User[] likes = {likedUser};
        when(message.getLikes()).thenReturn(likes);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.gtv__footer__like__button)).check(isVisible());
        onView(withId(R.id.gtv__footer__like__button)).check(hasText(activity.getString(R.string.glyph__like)));

        onView(withId(R.id.tv__footer__like__description)).check(isVisible());
        onView(withId(R.id.tv__footer__like__description)).check(hasText(likedUser.getDisplayName()));
    }

    @Test
    public void verifyICanSeeThatILikedMessage() {
        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, false);
        when(message.isLikedByThisUser()).thenReturn(true);
        when(message.isLiked()).thenReturn(true);
        User likedUser = mock(User.class);
        when(likedUser.getId()).thenReturn("456");
        when(likedUser.getDisplayName()).thenReturn("Barry");
        User[] likes = {likedUser};
        when(message.getLikes()).thenReturn(likes);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.gtv__footer__like__button)).check(isVisible());
        onView(withId(R.id.gtv__footer__like__button)).check(hasText(activity.getString(R.string.glyph__liked)));

        onView(withId(R.id.tv__footer__like__description)).check(isVisible());
        onView(withId(R.id.tv__footer__like__description)).check(hasText(likedUser.getDisplayName()));
    }

    @Test
    public void verifyICanTapMessageToRevealLikeButtonIfMessageHasNoLikes() throws InterruptedException {
        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, false);
        when(message.isLiked()).thenReturn(false);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.gtv__footer__like__button)).check(isGone());

        onView(withId(R.id.ltv__row_conversation__message)).check(isVisible());
        onView(withId(R.id.ltv__row_conversation__message)).perform(click());

        Thread.sleep(400);

        onView(withId(R.id.gtv__footer__like__button)).check(isVisible());
    }

    @Test
    public void verifyMyLastMessageThatHasNoLikesShowsDeliveryStatus() {
        IConversation conversation = createMockConversation(IConversation.Type.ONE_TO_ONE);

        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, true);
        when(message.getConversation()).thenReturn(conversation);
        when(message.isLiked()).thenReturn(false);
        when(message.isLastMessageFromSelf()).thenReturn(true);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.tv__footer__message_status)).check(isVisible());
        onView(withId(R.id.gtv__footer__like__button)).check(isGone());
        onView(withId(R.id.fldl_like_details)).check(isInvisible());
    }

    @Test
    public void verifyICanTapOnMyLastMessageThatHasNoLikesToRevealLikeButton() throws InterruptedException {
        IConversation conversation = createMockConversation(IConversation.Type.ONE_TO_ONE);

        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, true);
        when(message.getConversation()).thenReturn(conversation);
        when(message.isLikedByThisUser()).thenReturn(false);
        when(message.isLiked()).thenReturn(false);
        when(message.isLastMessageFromSelf()).thenReturn(true);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.gtv__footer__like__button)).check(isGone());
        onView(withId(R.id.ltv__row_conversation__message)).check(isVisible());
        onView(withId(R.id.ltv__row_conversation__message)).perform(click());

        Thread.sleep(400);

        onView(withId(R.id.gtv__footer__like__button)).check(isVisible());
    }

    @Test
    public void verifyMyLastMessageThatHasLikesShowsLikeDetails() {
        IConversation conversation = createMockConversation(IConversation.Type.ONE_TO_ONE);

        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, true);
        when(message.getConversation()).thenReturn(conversation);
        when(message.isLiked()).thenReturn(true);
        when(message.isLastMessageFromSelf()).thenReturn(true);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.tv__footer__message_status)).check(isInvisible());
        onView(withId(R.id.gtv__footer__like__button)).check(isGone());
        onView(withId(R.id.fldl_like_details)).check(isVisible());
    }

    @Test
    public void verifyMyMessageThatIsNotLastAndHasNoLikesHidesDeliveryStatusAndLikeButton() {
        IConversation conversation = createMockConversation(IConversation.Type.ONE_TO_ONE);

        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, true);
        when(message.getConversation()).thenReturn(conversation);
        when(message.isLiked()).thenReturn(false);
        when(message.isLastMessageFromSelf()).thenReturn(false);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.tv__footer__message_status)).check(isGone());
        onView(withId(R.id.gtv__footer__like__button)).check(isGone());
        onView(withId(R.id.fldl_like_details)).check(isGone());
    }

    @Test
    public void verifyICanTapOnMyMessageThatIsNotLastAndHasNoLikesToRevealDeliveryStatusAndLikeButton() throws InterruptedException {
        IConversation conversation = createMockConversation(IConversation.Type.ONE_TO_ONE);

        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, true);
        when(message.getConversation()).thenReturn(conversation);
        when(message.isLiked()).thenReturn(false);
        when(message.isLastMessageFromSelf()).thenReturn(false);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.tv__footer__message_status)).check(isGone());
        onView(withId(R.id.gtv__footer__like__button)).check(isGone());
        onView(withId(R.id.fldl_like_details)).check(isGone());

        onView(withId(R.id.ltv__row_conversation__message)).check(isVisible());
        onView(withId(R.id.ltv__row_conversation__message)).perform(click());

        Thread.sleep(400);

        onView(withId(R.id.tv__footer__message_status)).check(isVisible());
        onView(withId(R.id.gtv__footer__like__button)).check(isVisible());
        onView(withId(R.id.fldl_like_details)).check(isInvisible());
    }

    @Test
    public void verifyMyMessageThatIsNotLastAndHasLikesShowsLikeDetails() {
        IConversation conversation = createMockConversation(IConversation.Type.ONE_TO_ONE);

        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, true);
        when(message.getConversation()).thenReturn(conversation);
        when(message.isLiked()).thenReturn(true);
        when(message.isLastMessageFromSelf()).thenReturn(false);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.tv__footer__message_status)).check(isInvisible());
        onView(withId(R.id.gtv__footer__like__button)).check(isVisible());
        onView(withId(R.id.fldl_like_details)).check(isVisible());
    }

    @Test
    public void verifyMyLastMessageThatHasNoLikesHidesDeliveryStatusInGroupConversation() {
        IConversation conversation = createMockConversation(IConversation.Type.GROUP);

        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, true);
        when(message.getConversation()).thenReturn(conversation);
        when(message.isLiked()).thenReturn(false);
        when(message.isLastMessageFromSelf()).thenReturn(true);

        MessageAndSeparatorViewController messageAndSeparatorViewController = createMessageAndSeparatorViewController(message);
        messageAndSeparatorViewController.setModel(message, createMockSeparator());

        setView(messageAndSeparatorViewController.getView());

        onView(withId(R.id.tv__footer__message_status)).check(isGone());
        onView(withId(R.id.gtv__footer__like__button)).check(isGone());
        onView(withId(R.id.fldl_like_details)).check(isInvisible());
    }

    private MessageAndSeparatorViewController createMessageAndSeparatorViewController(Message message) {
        MessageViewsContainer messageViewsContainer = ViewControllerMockHelper.getMockMessageViewsContainer(activity);
        MessageViewController viewController = MessageViewControllerFactory.create(activity,
                                                                                   message,
                                                                                   messageViewsContainer);

        FooterViewController footerViewController = FooterViewControllerFactory.create(activity, message, messageViewsContainer);

        return new MessageAndSeparatorViewController(viewController,
                                                     footerViewController,
                                                     messageViewsContainer,
                                                     activity);
    }


    private Separator createMockSeparator() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("123");

        Message message = createMockMessage(Message.Type.TEXT, Message.Status.SENT, false);
        when(message.getUser()).thenReturn(mockUser);

        Separator separator = mock(Separator.class);
        when(separator.getNextMessage()).thenReturn(message);
        when(separator.getPreviousMessage()).thenReturn(message);
        return separator;
    }

    private Message createMockMessage(Message.Type type, Message.Status status, boolean sentByMe) {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn("1234");
        when(message.getMessageType()).thenReturn(type);
        when(message.getMessageStatus()).thenReturn(status);
        when(message.getBody()).thenReturn("Some message");
        when(message.isEdited()).thenReturn(false);
        when(message.getTime()).thenReturn(Instant.now());

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("123");
        when(mockUser.isMe()).thenReturn(sentByMe);
        when(message.getUser()).thenReturn(mockUser);

        return message;
    }

    private IConversation createMockConversation(IConversation.Type type) {
        IConversation conversation = mock(IConversation.class);
        when(conversation.getType()).thenReturn(type);
        return conversation;
    }
}
