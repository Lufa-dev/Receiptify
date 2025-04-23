import { Component, EventEmitter, Input, Output, ViewChild, ElementRef, OnInit, OnDestroy, forwardRef } from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

export interface SelectOption {
  label: string;
  value: any;
  group?: string;
}

@Component({
  selector: 'app-searchable-select',
  templateUrl: './searchable-select.component.html',
  styleUrls: ['./searchable-select.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SearchableSelectComponent),
      multi: true
    }
  ]
})
export class SearchableSelectComponent implements OnInit, OnDestroy, ControlValueAccessor {
  @Input() options: SelectOption[] = [];
  @Input() placeholder = 'Select an option';
  @Input() searchPlaceholder = 'Search...';
  @Input() grouped = false;
  @Input() disabled = false;
  @Input() clearAfterSelect = false;

  @Output() selectionChange = new EventEmitter<SelectOption | null>();

  @ViewChild('searchInput') searchInput!: ElementRef;

  isOpen = false;
  searchControl = new FormControl('');
  highlightedIndex = -1;
  selectedOption: SelectOption | null = null;

  private destroySubject = new Subject<void>();

  filteredOptions: SelectOption[] = [];
  filteredGroups: string[] = [];

  private onChange: (value: any) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private elementRef: ElementRef) {}

  ngOnInit(): void {
    this.filteredOptions = this.options;
    if (this.grouped) {
      this.updateFilteredGroups();
    }

    this.searchControl.valueChanges
      .pipe(
        debounceTime(200),
        distinctUntilChanged(),
        takeUntil(this.destroySubject)
      )
      .subscribe((searchTerm) => {
        this.filterOptions(searchTerm || '');
      });
  }

  ngOnDestroy(): void {
    this.destroySubject.next();
    this.destroySubject.complete();
  }

  writeValue(value: any): void {
    if (value) {
      const option = this.options.find(opt => opt.value === value);
      if (option) {
        this.selectedOption = option;
      }
    } else {
      this.selectedOption = null;
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  toggle(): void {
    if (this.disabled) return;

    if (this.isOpen) {
      this.close();
    } else {
      this.open();
    }
  }

  open(): void {
    if (this.disabled) return;

    this.isOpen = true;
    this.searchControl.setValue('');
    this.filterOptions('');
    this.highlightedIndex = -1;

    setTimeout(() => {
      if (this.searchInput) {
        this.searchInput.nativeElement.focus();
      }
    }, 0);
  }

  close(): void {
    this.isOpen = false;
    this.onTouched();
  }

  filterOptions(searchTerm: string): void {
    const searchTermLower = searchTerm.toLowerCase().trim();

    if (!searchTermLower) {
      this.filteredOptions = this.options;
    } else {
      this.filteredOptions = this.options.filter(option =>
        option.label.toLowerCase().includes(searchTermLower)
      );
    }

    if (this.grouped) {
      this.updateFilteredGroups();
    }

    this.highlightedIndex = this.filteredOptions.length > 0 ? 0 : -1;
  }

  updateFilteredGroups(): void {
    this.filteredGroups = Array.from(
      new Set(this.filteredOptions.map(option => option.group || ''))
    ).filter(group => group !== '');
  }

  getFilteredOptionsForGroup(group: string): SelectOption[] {
    return this.filteredOptions.filter(option => option.group === group);
  }

  getGlobalIndex(group: string, localIndex: number): number {
    let index = 0;
    for (const g of this.filteredGroups) {
      if (g === group) {
        return index + localIndex;
      }
      index += this.getFilteredOptionsForGroup(g).length;
    }
    return -1;
  }

  selectOption(option: SelectOption): void {
    if (!this.clearAfterSelect) {
      this.selectedOption = option;
      this.onChange(option.value);
    }

    this.selectionChange.emit(option);
    this.close();

    // If clearAfterSelect is true, reset the selection after emitting the event
    if (this.clearAfterSelect) {
      setTimeout(() => {
        this.selectedOption = null;
        this.onChange(null);
      }, 0);
    }
  }

  isSelected(option: SelectOption): boolean {
    return this.selectedOption?.value === option.value;
  }

  onKeydown(event: KeyboardEvent): void {
    if (this.disabled) return;

    switch (event.key) {
      case 'Enter':
      case ' ':
        event.preventDefault();
        if (!this.isOpen) {
          this.open();
        } else if (this.highlightedIndex >= 0) {
          this.selectOption(this.filteredOptions[this.highlightedIndex]);
        }
        break;
      case 'Escape':
        if (this.isOpen) {
          event.preventDefault();
          this.close();
        }
        break;
      case 'ArrowDown':
        event.preventDefault();
        if (!this.isOpen) {
          this.open();
        } else {
          this.highlightedIndex = Math.min(this.highlightedIndex + 1, this.filteredOptions.length - 1);
        }
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.highlightedIndex = Math.max(this.highlightedIndex - 1, 0);
        break;
    }
  }

  onSearchKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.close();
    } else if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.highlightedIndex = Math.min(this.highlightedIndex + 1, this.filteredOptions.length - 1);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.highlightedIndex = Math.max(this.highlightedIndex - 1, 0);
    } else if (event.key === 'Enter' && this.highlightedIndex >= 0) {
      event.preventDefault();
      this.selectOption(this.filteredOptions[this.highlightedIndex]);
    }
  }
}
