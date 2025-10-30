import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subscription, of } from 'rxjs';
import { finalize, catchError } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faSpinner, faMagnifyingGlass } from '@fortawesome/free-solid-svg-icons';

import { Uf } from 'app/entities/enumerations/uf.model';
import { IAddress } from '../address.model';
import { AddressService } from '../service/address.service';
import { AddressFormGroup, AddressFormService } from './address-form.service';
import { CepLookupService } from 'app/entities/address/service/cep-lookup.service';
import { NotificationService } from 'app/shared/notification/notification.service';

@Component({
  selector: 'jhi-address-update',
  templateUrl: './address-update.component.html',
  styleUrls: ['./address-update.component.scss'],
  standalone: true,
  imports: [SharedModule, FormsModule, ReactiveFormsModule, FontAwesomeModule],
})
export class AddressUpdateComponent implements OnInit {
  isSaving = false;
  titulo = 'Create a Address';
  address: IAddress | null = null;
  ufValues = Object.keys(Uf);

  // 🔄 Estados de Loading UX Avançados
  isBuscandoCep = false;
  isLoadingFields = false;
  loadingFields: string[] = [];
  fieldStates: Record<string, 'loading' | 'success' | 'error' | 'normal'> = {};

  spinnerIcon = faSpinner;
  searchIcon = faMagnifyingGlass;

  protected addressService = inject(AddressService);
  protected addressFormService = inject(AddressFormService);
  protected activatedRoute = inject(ActivatedRoute);
  protected notificationService = inject(NotificationService);

  editForm: AddressFormGroup = this.addressFormService.createAddressFormGroup();

  constructor(protected cepLookupService: CepLookupService) {}
  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ address }) => {
      this.address = address;
      if (address) {
        this.updateForm(address);
        this.titulo = 'Edit a Address';
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const address = this.addressFormService.getAddress(this.editForm);
    if (address.id !== null) {
      this.subscribeToSaveResponse(this.addressService.update(address));
    } else {
      this.subscribeToSaveResponse(this.addressService.create(address));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAddress>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.notificationService.info('Endereço salvo com sucesso!');
    this.previousState();
  }
  protected onSaveError(): void {
    // TODO: Handle save error
  }
  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(address: IAddress): void {
    this.address = address;
    this.addressFormService.resetForm(this.editForm, address);
  }

  onBuscarCep(): void {
    const cepControl = this.editForm.get('cep');
    if (!cepControl) {
      console.warn('CEP field not found in the address form');
      return;
    }
    let cepValue: string | null = cepControl.value ?? null;
    if (!cepValue) {
      return;
    }
    cepValue = cepValue.replace(/\D/g, '');
    if (cepValue.length !== 8) {
      cepControl.setErrors({ invalidCep: true });
      return;
    }

    cepControl.setErrors(null);
    this.setFieldState('cep', 'normal');

    this.startCepLookupLoading();

    this.cepLookupService
      .lookup(cepValue, true)
      .pipe(
        catchError((error: any) => {
          console.error('Error fetching CEP:', error);

          if (error.status === 400) {
            cepControl.setErrors({ invalidCep: true });
          } else if (error.status === 404) {
            cepControl.setErrors({ notFound: true });
            this.setFieldState('cep', 'error');
            this.notificationService.warning('CEP não encontrado');
          } else if (error.status === 502) {
            cepControl.setErrors({ serviceUnavailable: true });
            this.setFieldState('cep', 'error');
          } else {
            cepControl.setErrors({ unknownError: true });
            this.setFieldState('cep', 'error');
          }

          this.stopCepLookupLoading();
          return of();
        }),
        finalize(() => {
          this.stopCepLookupLoading();
        }),
      )
      .subscribe({
        next: (address: IAddress) => {
          if (address) {
            const currentNumber = this.editForm.get('number')?.value;
            let currentComplement = this.editForm.get('complement')?.value;
            if (address.complement != null) {
              currentComplement = address.complement;
            }

            this.notificationService.success(`CEP ${cepValue ?? ''} encontrado com sucesso!`);

            this.animateFieldFill(address, currentNumber, currentComplement);
          }
        },
        error: () => {
          // eslint-disable-next-line no-console
          console.log('Fallback error handler - should not be called');
        },
      });
  }

  private startCepLookupLoading(): void {
    this.isBuscandoCep = true;
    this.isLoadingFields = true;
    this.loadingFields = ['street', 'district', 'city', 'uf'];

    this.loadingFields.forEach(field => {
      this.setFieldState(field, 'loading');
    });
  }

  private stopCepLookupLoading(): void {
    this.isBuscandoCep = false;
    this.isLoadingFields = false;
    this.loadingFields = [];
  }

  private animateFieldFill(address: IAddress, currentNumber: any, currentComplement: any): void {
    const fieldsToFill = [
      { name: 'street', value: address.street ?? '', delay: 100 },
      { name: 'district', value: address.district ?? '', delay: 200 },
      { name: 'city', value: address.city ?? '', delay: 300 },
      { name: 'uf', value: address.uf ?? null, delay: 400 },
    ];

    fieldsToFill.forEach(({ name, value, delay }) => {
      setTimeout(() => {
        this.editForm.get(name)?.setValue(value);
        this.setFieldState(name, 'success');

        setTimeout(() => {
          this.setFieldState(name, 'normal');
        }, 2000);
      }, delay);
    });

    setTimeout(() => {
      this.editForm.patchValue({
        number: currentNumber || '',
        complement: currentComplement ?? '',
      });
      this.setFieldState('cep', 'success');

      setTimeout(() => {
        this.setFieldState('cep', 'normal');
      }, 2000);
    }, 500);
  }

  private setFieldState(fieldName: string, state: 'loading' | 'success' | 'error' | 'normal'): void {
    this.fieldStates[fieldName] = state;
  }

  getFieldClass(fieldName: string): string {
    const state = this.fieldStates[fieldName] || 'normal';
    const baseClass = 'form-control';

    switch (state) {
      case 'loading':
        return `${baseClass} field-loading`;
      case 'success':
        return `${baseClass} field-success`;
      case 'error':
        return `${baseClass} field-error`;
      default:
        return baseClass;
    }
  }

  isFieldLoading(fieldName: string): boolean {
    return this.fieldStates[fieldName] === 'loading';
  }

  getBtnCepClass(): string {
    let baseClass = 'btn btn-outline-primary';
    if (this.isBuscandoCep) {
      baseClass += ' btn-loading';
    }
    return baseClass;
  }

  formatCep(): void {
    const cepControl = this.editForm.get('cep');
    if (!cepControl) {
      return;
    }
    const raw = (cepControl.value ?? '') as string;
    const digits = raw.replace(/\D/g, '').slice(0, 8);
    let formatted = digits;
    if (digits.length > 5) {
      formatted = digits.slice(0, 5) + '-' + digits.slice(5);
    }
    cepControl.setValue(formatted, { emitEvent: false });
  }
}
