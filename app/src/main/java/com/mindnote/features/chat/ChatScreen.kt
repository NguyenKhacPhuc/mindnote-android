package com.mindnote.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.mindnote.R
import com.mindnote.design.AiAvatar
import com.mindnote.design.HSpace
import com.mindnote.design.HairlineDivider
import com.mindnote.design.MindNoteTheme
import com.mindnote.domain.model.ChatMessage
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChatScreen(
    conversationId: String,
    initialText: String? = null,
    onBack: () -> Unit,
    vm: ChatViewModel = koinViewModel(parameters = { parametersOf(conversationId) }),
) {
    val liveMessages by vm.liveMessages.collectAsStateWithLifecycle()
    val history = vm.historyPager.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                ChatEffect.NavigateBack -> onBack()
            }
        }
    }

    var initialSent by rememberSaveable(conversationId) { mutableStateOf(false) }
    LaunchedEffect(conversationId, initialText) {
        if (!initialSent && !initialText.isNullOrBlank()) {
            initialSent = true
            vm.send(ChatIntent.SendMessage(initialText))
        }
    }

    var inputText by remember { mutableStateOf("") }

    val isInitialLoading = history.loadState.refresh is LoadState.Loading &&
        history.itemCount == 0 &&
        liveMessages.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindNoteTheme.colors.bg)
            .navigationBarsPadding(),
    ) {
        Spacer(Modifier.height(44.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = MindNoteTheme.colors.text,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { vm.send(ChatIntent.GoBack) },
            )
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Text(
                    text = stringResource(R.string.chat_default_title),
                    style = MindNoteTheme.typography.titleSmall,
                    color = MindNoteTheme.colors.text,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }

        HairlineDivider()

        if (isInitialLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = MindNoteTheme.colors.accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(28.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                reverseLayout = true,
            ) {
                // Session-local messages at the bottom. LazyColumn(reverseLayout=true) renders
                // index 0 at the bottom, so reverse to keep chronological order on screen.
                items(
                    items = liveMessages.asReversed(),
                    key = { it.id },
                    contentType = { "chat-msg" },
                ) { message -> MessageRow(message) }

                // Paged history above — server returns newest-first, matching reverseLayout.
                items(
                    count = history.itemCount,
                    key = history.itemKey { it.id },
                    contentType = history.itemContentType { "chat-msg" },
                ) { index ->
                    val message = history[index] ?: return@items
                    MessageRow(message)
                }

                if (history.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = MindNoteTheme.colors.accent,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }

        HairlineDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MindNoteTheme.colors.bg)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MindNoteTheme.colors.surface)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = stringResource(R.string.chat_input_placeholder),
                        style = MindNoteTheme.typography.body,
                        color = MindNoteTheme.colors.textSubtle,
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MindNoteTheme.colors.text,
                        fontSize = MindNoteTheme.typography.body.fontSize,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            HSpace(8)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MindNoteTheme.colors.accent)
                    .clickable {
                        val toSend = inputText
                        inputText = ""
                        vm.send(ChatIntent.SendMessage(toSend))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowUpward,
                    contentDescription = stringResource(R.string.chat_send_cd),
                    tint = MindNoteTheme.colors.bg,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun MessageRow(message: ChatMessage) {
    when (message.role) {
        ChatMessage.Role.User -> UserBubble(message)
        ChatMessage.Role.Assistant -> AssistantBubble(message)
    }
}

@Composable
private fun UserBubble(message: ChatMessage) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MindNoteTheme.colors.accentSoft)
                .padding(12.dp),
        ) {
            Text(
                text = message.text,
                style = MindNoteTheme.typography.body,
                color = MindNoteTheme.colors.text,
            )
        }
    }
}

@Composable
private fun AssistantBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AiAvatar(size = 20)
            HSpace(8)
            Text(
                text = stringResource(R.string.chat_assistant_label),
                style = MindNoteTheme.typography.caption,
                color = MindNoteTheme.colors.textMuted,
            )
        }

        Text(
            text = message.text,
            style = MindNoteTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 22.sp,
            ),
            color = MindNoteTheme.colors.text,
        )

        if (message.isThinking) {
            Text(
                text = stringResource(R.string.chat_thinking),
                style = MindNoteTheme.typography.bodySmall,
                color = MindNoteTheme.colors.textMuted,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}
