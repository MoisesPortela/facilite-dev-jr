import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IAddress } from '../address.model';

@Injectable({ providedIn: 'root' })
export class CepLookupService {
  private resourceUrl: string;

  constructor(
    private http: HttpClient,
    private applicationConfigService: ApplicationConfigService,
  ) {
    this.resourceUrl = this.applicationConfigService.getEndpointFor('api/cep');
  }

  lookup(cep: string, skipGlobalErrorHandler = false): Observable<IAddress> {
    const headers = skipGlobalErrorHandler ? new HttpHeaders({ 'Skip-Global-Error-Handler': 'true' }) : undefined;

    return this.http.get<IAddress>(`${this.resourceUrl}/${cep}`, { headers });
  }
}
