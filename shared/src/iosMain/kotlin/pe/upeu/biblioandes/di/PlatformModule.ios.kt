package pe.upeu.biblioandes.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Sin dependencias exclusivas de iOS por ahora.
}
