package com.khepilabs.khepitube.ui.screens.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.data.backup.ImportKind
import com.khepilabs.khepitube.data.backup.ImportSource
import com.khepilabs.khepitube.ui.components.shared.KhepiNavRow
import com.khepilabs.khepitube.ui.components.shared.KhepiRowGroup
import com.khepilabs.khepitube.ui.components.shared.KhepiSheetHeader
import com.khepilabs.khepitube.ui.components.shared.khepiRowGroupShape
import com.khepilabs.khepitube.ui.components.shared.rememberKhepiSheetState

private val SheetBottomPadding = 16.dp

/** Everything [source] can import, the same list Settings shows under that app. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImportSourceSheet(
    source: ImportSource,
    onImport: (ImportKind) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberKhepiSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        KhepiSheetHeader(
            title = stringResource(source.titleRes),
            onClose = onDismiss,
            showDragHandle = false,
        )
        KhepiRowGroup(Modifier.verticalScroll(rememberScrollState()).padding(bottom = SheetBottomPadding)) {
            source.kinds.forEachIndexed { index, kind ->
                KhepiNavRow(
                    title = stringResource(kind.titleRes),
                    supportingText = stringResource(kind.descriptionRes),
                    leadingIcon = Icons.Outlined.FileOpen,
                    showChevron = false,
                    shape = khepiRowGroupShape(index, source.kinds.size),
                    onClick = { onImport(kind) },
                )
            }
        }
    }
}
