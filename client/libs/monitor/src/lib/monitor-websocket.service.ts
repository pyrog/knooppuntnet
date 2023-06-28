import { DOCUMENT } from '@angular/common';
import { signal } from '@angular/core';
import { Injectable } from '@angular/core';
import { Inject } from '@angular/core';
import { MonitorRouteUpdate } from '@api/common/monitor/monitor-route-update';
import { MonitorRouteUpdateStatusMessage } from '@api/common/monitor/monitor-route-update-status-message';
import { WebSocketSubject } from 'rxjs/webSocket';
import { webSocket } from 'rxjs/webSocket';
import { MonitorRouteSaveStep } from './route/monitor-route-save-step';

@Injectable()
export class MonitorWebsocketService {
  readonly #steps = signal<MonitorRouteSaveStep[]>([]);
  readonly steps = this.#steps.asReadonly();

  readonly #errors = signal<string[]>([]);
  readonly errors = this.#errors.asReadonly();

  readonly #done = signal<boolean>(false);
  readonly done = this.#done.asReadonly();

  #webSocketSubject: WebSocketSubject<any>;

  constructor(@Inject(DOCUMENT) private document) {}

  sendCommand(command: MonitorRouteUpdate) {
    let protocol = 'wss';
    if (document.location.protocol === 'http:') {
      protocol = 'ws';
    }
    const host = document.location.host;
    const url = `${protocol}://${host}/websocket`;

    this.#webSocketSubject = webSocket({
      url,
      // serializer: (x) => {
      //   console.log(['websocket serializer', x]);
      //   return x;
      // },
      deserializer: (messageEvent) => {
        return messageEvent.data;
      },
      openObserver: {
        next: () => {
          this.#log('websocket connection open');
        },
      },
    });
    this.#webSocketSubject.subscribe({
      next: (msg) => {
        this.#logArgs(['websocket message received', msg]);

        const message: MonitorRouteUpdateStatusMessage = JSON.parse(msg);

        if (message.commands) {
          const stepsArray = this.#steps();
          message.commands.forEach((command) => {
            if (command.action === 'step-add') {
              let description = '';
              if (command.description) {
                description = command.description;
              } else {
                if (command.stepId === 'prepare') {
                  description = $localize`:@@monitor.update-command.prepare:Prepare`;
                } else if (command.stepId === 'analyze-route-structure') {
                  description = $localize`:@@monitor.update-command.analyze-route-structure:Analyze route structure`;
                } else if (command.stepId === 'load-gpx') {
                  description = $localize`:@@monitor.update-command.load-gpx:Load gpx`;
                } else if (command.stepId === 'analyze') {
                  description = $localize`:@@monitor.update-command.analyze:Analyze route deviations`;
                } else if (command.stepId === 'save') {
                  description = $localize`:@@monitor.update-command.save:Save`;
                } else {
                  description = 'TODO translate: ' + command.stepId;
                }
              }
              const step: MonitorRouteSaveStep = {
                stepId: command.stepId,
                status: 'todo',
                description,
              };
              stepsArray.push(step);
            } else if (command.action === 'step-active') {
              stepsArray.forEach((step) => {
                if (step.stepId === command.stepId) {
                  step.status = 'busy';
                } else {
                  if (step.status === 'busy') {
                    step.status = 'done';
                  }
                }
              });
            } else if (command.action === 'step-done') {
              stepsArray.forEach((step) => {
                if (step.stepId === command.stepId) {
                  step.status = 'done';
                }
              });
            }
          });
          this.#steps.set(stepsArray);
        }
      },
      error: (err) => {
        this.#logArgs(['websocket error', err]);
      },
      complete: () => {
        this.#log('websocket complete');
        this.#done.set(true);
      },
    });
    this.#webSocketSubject.next(command);
  }

  reset() {
    this.#done.set(false);
    this.#steps.set([]);
  }

  #complete(): void {
    this.#webSocketSubject.complete();
  }

  #log(message: string): void {
    console.log(`${this.#now()} ${message}`);
  }

  #logArgs(args: any[]): void {
    const coll: any[] = [this.#now()];
    args.forEach((arg) => coll.push(arg));
    console.log(coll);
  }

  #now(): string {
    const date = new Date();
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const dayPart = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    const millis = date.getMilliseconds().toString().padStart(3, '0');
    return `${year}-${month}-${dayPart} ${hours}:${minutes}:${seconds}.${millis}`;
  }
}