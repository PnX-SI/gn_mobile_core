package fr.geonature.sync.settings

import fr.geonature.commons.settings.AppSettingsViewModel
import fr.geonature.commons.settings.IAppSettingsManager

/**
 * [AppSettings] view model.
 *
 * @author S. Grimault
 */
class AppSettingsViewModel(appSettingsManager: IAppSettingsManager<AppSettings>) :
    AppSettingsViewModel<AppSettings>(appSettingsManager)
