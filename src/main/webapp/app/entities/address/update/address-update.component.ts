import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subscription, firstValueFrom } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faSpinner, faMagnifyingGlass } from '@fortawesome/free-solid-svg-icons';

import { Uf } from 'app/entities/enumerations/uf.model';
import { IAddress } from '../address.model';
import { AddressService } from '../service/address.service';
import { AddressFormGroup, AddressFormService } from './address-form.service';
import { CepLookupService } from 'app/entities/address/service/cep-lookup.service';

@Component({
  selector: 'jhi-address-update',
  templateUrl: './address-update.component.html',
  standalone: true,
  imports: [SharedModule, FormsModule, ReactiveFormsModule, FontAwesomeModule],
})
export class AddressUpdateComponent implements OnInit {
  isSaving = false;
  address: IAddress | null = null;
  ufValues = Object.keys(Uf);
  isBuscandoCep = false;

  // FontAwesome icons
  spinnerIcon = faSpinner;
  searchIcon = faMagnifyingGlass;

  protected addressService = inject(AddressService);
  protected addressFormService = inject(AddressFormService);
  protected activatedRoute = inject(ActivatedRoute);

  constructor(protected cepLookupService: CepLookupService) {}

  editForm: AddressFormGroup = this.addressFormService.createAddressFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ address }) => {
      this.address = address;
      if (address) {
        this.updateForm(address);
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
    this.previousState();
  }
  protected onSaveError(): void {}
  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(address: IAddress): void {
    this.address = address;
    this.addressFormService.resetForm(this.editForm, address);
  }

  async onBuscarCep(): Promise<void> {
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

    this.isBuscandoCep = true;
    try {
      const address: IAddress = await firstValueFrom(this.cepLookupService.lookup(cepValue));
      const currentNumber = this.editForm.get('number')?.value;
      let currentComplement = this.editForm.get('complement')?.value;
      if (address.complement != null) {
        currentComplement = address.complement;
      }
      this.editForm.patchValue({
        street: address.street ?? '',
        complement: currentComplement ?? '',
        district: address.district ?? '',
        city: address.city ?? '',
        uf: address.uf ?? null,
        number: currentNumber || '',
      });
    } catch (error: any) {
      console.error('Error fetching CEP:', error);
      if (error.status === 400) {
        cepControl.setErrors({ invalidCep: true });
      } else if (error.status === 404) {
        cepControl.setErrors({ notFound: true });
      } else if (error.status === 502) {
        cepControl.setErrors({ serviceUnavailable: true });
      } else {
        cepControl.setErrors({ unknownError: true });
      }
    } finally {
      this.isBuscandoCep = false;
    }
  }
  formatCep(): void {
    const cepControl = this.editForm.get('cep');
    if (!cepControl) {
      return;
    }
    const raw: string = (cepControl.value ?? '') as string;
    const digits = raw.replace(/\D/g, '').slice(0, 8);
    let formatted = digits;
    if (digits.length > 5) {
      formatted = digits.slice(0, 5) + '-' + digits.slice(5);
    }
    cepControl.setValue(formatted, { emitEvent: false });
  }
}
