import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Meter {
  private base = 'http://localhost:8080/api/meter';

  constructor(private http: HttpClient) {}

  readEnergy(): Observable<string>          { return this.http.get(this.base + '/energy',  { responseType: 'text' }); }
  readClock(): Observable<string>           { return this.http.get(this.base + '/clock',   { responseType: 'text' }); }
  relayStatus(): Observable<string>         { return this.http.get(this.base + '/status',  { responseType: 'text' }); }
  disconnectRelay(): Observable<string>     { return this.http.get(this.base + '/disconnect', { responseType: 'text' }); }
  connectRelay(): Observable<string>        { return this.http.get(this.base + '/connect',    { responseType: 'text' }); }
  openConnection(): Observable<string>      { return this.http.get(this.base + '/mediaConnect', { responseType: 'text' }); }
  closeConnection(): Observable<string>     { return this.http.get(this.base + '/mediaDisconnect', { responseType: 'text' }); }
}
