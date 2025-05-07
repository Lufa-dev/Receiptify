import {Directive, ElementRef, EventEmitter, HostListener, OnDestroy, Output} from '@angular/core';

@Directive({
  selector: '[clickOutside]'
})
export class ClickOutsideDirective implements OnDestroy {
  @Output() clickOutside = new EventEmitter<void>();

  private documentClickListener: Function;

  constructor(private elementRef: ElementRef) {
    // Add listener in constructor
    this.documentClickListener = this.onClick.bind(this);
    document.addEventListener('click', this.documentClickListener as EventListener);
  }

  ngOnDestroy(): void {
    // Remove listener when directive is destroyed
    document.removeEventListener('click', this.documentClickListener as EventListener);
  }

  private onClick(event: Event): void {
    const target = event.target as HTMLElement;
    const clickedInside = this.elementRef.nativeElement.contains(target);
    if (!clickedInside) {
      this.clickOutside.emit();
    }
  }
}
