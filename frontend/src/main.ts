import { bootstrapApplication } from '@angular/platform-browser';
import { AppKomponenta } from './app/app.component';
import { appConfig } from './app/app.config';

bootstrapApplication(AppKomponenta, appConfig).catch(err => console.error(err));
