package com.mindnote.features.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindnote.R
import com.mindnote.design.HSpace
import com.mindnote.design.HairlineDivider
import com.mindnote.design.MindNoteTheme
import com.mindnote.design.TagChip
import com.mindnote.design.VSpace
import org.koin.androidx.compose.koinViewModel

@Composable
fun CaptureScreen(
    onDismiss: () -> Unit,
    onSaved: (String) -> Unit,
    vm: CaptureViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = MindNoteTheme.snackbar

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                CaptureEffect.Dismiss -> onDismiss()
                is CaptureEffect.Saved -> onSaved(effect.noteId)
                is CaptureEffect.ShowError -> snackbar.showSnackbar(effect.message)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindNoteTheme.colors.bg)
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VSpace(44)
            TopBar(
                canSave = (state.title.isNotBlank() || state.body.isNotBlank()) && !state.isSaving,
                isSaving = state.isSaving,
                onCancel = { vm.send(CaptureIntent.Cancel) },
                onSave = { vm.send(CaptureIntent.Save) },
            )
            HairlineDivider()
            VSpace(24)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                BasicTextField(
                    value = state.title,
                    onValueChange = { vm.send(CaptureIntent.UpdateTitle(it)) },
                    textStyle = MindNoteTheme.typography.h2.copy(color = MindNoteTheme.colors.text),
                    cursorBrush = SolidColor(MindNoteTheme.colors.accent),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (state.title.isEmpty()) {
                            Text(
                                text = stringResource(R.string.capture_title_placeholder),
                                style = MindNoteTheme.typography.h2,
                                color = MindNoteTheme.colors.textSubtle,
                            )
                        }
                        inner()
                    },
                )
                VSpace(12)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MindNoteTheme.colors.tagBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = state.dateLabel,
                            style = MindNoteTheme.typography.tracked,
                            color = MindNoteTheme.colors.textSubtle,
                        )
                    }
                    state.tags.forEach { tag ->
                        RemovableTagChip(
                            label = tag,
                            onRemove = { vm.send(CaptureIntent.RemoveTag(tag)) },
                        )
                    }
                    Box(modifier = Modifier.clickable { vm.send(CaptureIntent.AddTag) }) {
                        TagChip(label = stringResource(R.string.capture_add_tag), active = false)
                    }
                }
                VSpace(20)
                HairlineDivider()
                VSpace(20)
                BasicTextField(
                    value = state.body,
                    onValueChange = { vm.send(CaptureIntent.UpdateBody(it)) },
                    textStyle = MindNoteTheme.typography.body.copy(
                        color = MindNoteTheme.colors.text,
                        lineHeight = 24.sp,
                    ),
                    cursorBrush = SolidColor(MindNoteTheme.colors.accent),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (state.body.isEmpty()) {
                            Text(
                                text = stringResource(R.string.capture_body_placeholder),
                                style = MindNoteTheme.typography.body.copy(lineHeight = 24.sp),
                                color = MindNoteTheme.colors.textSubtle,
                            )
                        }
                        inner()
                    },
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MindNoteTheme.colors.bg),
        ) {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MindNoteTheme.colors.surface)
                        .border(1.dp, MindNoteTheme.colors.border, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MindNoteTheme.colors.accent,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.capture_ai_tip),
                        style = MindNoteTheme.typography.caption.copy(lineHeight = 18.sp),
                        color = MindNoteTheme.colors.textMuted,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            VSpace(12)
            HairlineDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { vm.send(CaptureIntent.AttachLink) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Link,
                        contentDescription = stringResource(R.string.capture_attach_link_cd),
                        tint = MindNoteTheme.colors.textMuted,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            VSpace(24)
        }

        val tagInput = state.tagInput
        if (tagInput != null) {
            AddTagDialog(
                value = tagInput,
                onChange = { vm.send(CaptureIntent.UpdateTagInput(it)) },
                onConfirm = { vm.send(CaptureIntent.ConfirmTag) },
                onDismiss = { vm.send(CaptureIntent.CancelTag) },
            )
        }
    }
}

@Composable
private fun TopBar(canSave: Boolean, isSaving: Boolean, onCancel: () -> Unit, onSave: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.action_cancel),
            style = MindNoteTheme.typography.label,
            color = MindNoteTheme.colors.textMuted,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable(enabled = !isSaving, onClick = onCancel),
        )
        Text(
            text = stringResource(R.string.capture_title),
            style = MindNoteTheme.typography.titleSmall,
            color = MindNoteTheme.colors.text,
            modifier = Modifier.align(Alignment.Center),
        )
        if (isSaving) {
            CircularProgressIndicator(
                color = MindNoteTheme.colors.accent,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(18.dp),
            )
        } else {
            Text(
                text = stringResource(R.string.action_save),
                style = MindNoteTheme.typography.label,
                color = if (canSave) MindNoteTheme.colors.accent
                else MindNoteTheme.colors.accent.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(enabled = canSave, onClick = onSave),
            )
        }
    }
}

@Composable
private fun RemovableTagChip(label: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MindNoteTheme.colors.tagBg)
            .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MindNoteTheme.typography.tracked,
            color = MindNoteTheme.colors.textMuted,
        )
        HSpace(4)
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.capture_tag_remove_cd),
            tint = MindNoteTheme.colors.textSubtle,
            modifier = Modifier
                .size(14.dp)
                .clickable(onClick = onRemove),
        )
    }
}

@Composable
private fun AddTagDialog(
    value: String,
    onChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.capture_tag_dialog_title),
                style = MindNoteTheme.typography.titleSmall,
                color = MindNoteTheme.colors.text,
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MindNoteTheme.colors.surface)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.capture_tag_dialog_placeholder),
                        style = MindNoteTheme.typography.body,
                        color = MindNoteTheme.colors.textSubtle,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onChange,
                    singleLine = true,
                    textStyle = MindNoteTheme.typography.body.copy(color = MindNoteTheme.colors.text),
                    cursorBrush = SolidColor(MindNoteTheme.colors.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = value.isNotBlank(),
            ) {
                Text(
                    text = stringResource(R.string.action_add),
                    color = if (value.isNotBlank()) MindNoteTheme.colors.accent
                    else MindNoteTheme.colors.accent.copy(alpha = 0.4f),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel), color = MindNoteTheme.colors.textMuted)
            }
        },
        containerColor = MindNoteTheme.colors.bg,
    )
}
