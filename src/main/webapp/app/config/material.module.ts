import { NgModule } from '@angular/core';
import { MatSnackBarModule } from '@angular/material/snack-bar';

@NgModule({
  imports: [MatSnackBarModule],
  exports: [MatSnackBarModule],
})
export class MaterialModule {}
