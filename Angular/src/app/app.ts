import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Dashboard } from "../dashboard/dashboard";
import { Dashboard2 } from "../dashboard2/dashboard2";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Dashboard, Dashboard2],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('MeterAngular');
}
