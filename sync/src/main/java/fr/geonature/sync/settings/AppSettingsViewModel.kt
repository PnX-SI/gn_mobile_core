package fr.geonature.sync.settings

import android.app.Application
import fr.geonature.commons.settings.AppSettingsViewModel
import fr.geonature.sync.settings.io.OnAppSettingsJsonReaderListenerImpl

/**
 * [AppSettings] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSettingsViewModel(application: Application) : AppSettingsViewModel<AppSettings>(application,
                                                                                         OnAppSettingsJsonReaderListenerImpl())