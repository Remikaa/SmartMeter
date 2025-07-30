import { CommonModule } from '@angular/common';
import { Component , OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Meter } from '../app/services/meter';

@Component({
  selector: 'app-dashboard2',
  imports: [FormsModule, CommonModule],
  templateUrl: './dashboard2.html',
  styleUrls: ['./dashboard2.css']
})


export class Dashboard2 implements OnInit, OnDestroy {
   isConnected = false;
  
  serialConfig = {
    comPort: 'COM0',
    baudRate: 9600,
    dataBits: 8,
    parity: 'None',
    stopBits: '1'
  };

  clientConfig = {
    clientAddr: null,
    serverAddr: null,
    authType: 'None',
    password: ''
  };

  meterData = {
    energy: '0.00',
    time: '--:--'
  };

  relayState = {
    state: 'OFF',
    output: 'LOW'
  };

  consoleOutput = 'System initialized...\nReady for connection.\nWaiting for commands...';

  comPorts = ['COM0', 'COM1', 'COM2', 'COM3', 'COM4', 'COM5', 'COM6', 'COM7', 'COM8', 'COM9', 'COM10'];
  baudRates = [300, 600, 1800, 2400, 4800, 9600];
  parityOptions = ['Odd', 'Even', 'Mark', 'Space', 'None'];
  stopBitsOptions = ['1', '1.5', '2'];
  authTypes = ['None', 'Low', 'High', 'High MD5', 'High SHA1', 'High GMAC', 'High SHA256', 'High ECDSA'];

  private dataUpdateInterval: any;
  constructor(private meter: Meter) {}

  ngOnInit() {
    
  }

  ngOnDestroy() {
    if (this.dataUpdateInterval) {
      clearInterval(this.dataUpdateInterval);
    }
  }

  // toggleConnection() {
  //   this.isConnected = !this.isConnected;
  //   this.addToConsole(`Connection ${this.isConnected ? 'established' : 'closed'}`);
  // }

  // applySettings() {
  //   this.addToConsole('Client settings applied successfully');
  // }

  // syncData() {
  //   this.addToConsole('Syncing meter data...');
  //   this.updateMeterData();
  // }

  // disconnectRelay() {
  //   this.relayState.state = 'OFF';
  //   this.relayState.output = 'LOW';
  //   this.addToConsole('Relay disconnected');
  // }

  // reconnectRelay() {
  //   this.relayState.state = 'ON';
  //   this.relayState.output = 'HIGH';
  //   this.addToConsole('Relay reconnected');
  // }

  private updateMeterData() {
    this.meterData.energy = (Math.random() * 1000).toFixed(2);
    this.meterData.time = new Date().toLocaleTimeString();
  }

  private addToConsole(message: string) {
    const timestamp = new Date().toLocaleTimeString();
    this.consoleOutput += `\n[${timestamp}] ${message}`;
  }


    async toggleConnection() {
    this.isConnected = !this.isConnected;
    this.addToConsole(`Attempting ${this.isConnected ? 'open' : 'close'} …`);

    try {
      const res = this.isConnected
        ? await firstValueFrom(this.meter.openConnection())
        : await firstValueFrom(this.meter.closeConnection());
      this.addToConsole(res);
    } catch (err: any) {
      this.addToConsole('❌ ' + err.message);
      this.isConnected = false;
    }
  }

  async syncData() {
    this.addToConsole('Syncing meter data …');
    try {
      const energy = await firstValueFrom(this.meter.readEnergy());
      const clock  = await firstValueFrom(this.meter.readClock());
      this.addToConsole(energy);
      this.addToConsole('Clock: ' + clock);

      // Parse and update UI
      this.meterData.energy = energy.split('\n')[1]   // quick & dirty
                                   .split(':')[1].trim();
      this.meterData.time   = clock;
    } catch (err: any) {
      this.addToConsole('❌ ' + err.message);
    }
  }

  async disconnectRelay() {
    try {
      const msg = await firstValueFrom(this.meter.disconnectRelay());
      this.addToConsole(msg);
      this.relayState.state = 'OFF';
    } catch (err: any) {
      this.addToConsole('❌ ' + err.message);
    }
  }

  async reconnectRelay() {
    try {
      const msg = await firstValueFrom(this.meter.connectRelay());
      this.addToConsole(msg);
      this.relayState.state = 'ON';
    } catch (err: any) {
      this.addToConsole('❌ ' + err.message);
    }
  }

  
}
