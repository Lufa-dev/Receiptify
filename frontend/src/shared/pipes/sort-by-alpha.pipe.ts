import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'sortByAlpha'
})
export class SortByAlphaPipe implements PipeTransform {
  transform<T>(array: T[], key: keyof T): T[] {
    if (!array || !Array.isArray(array)) {
      return array;
    }

    return array.slice().sort((a, b) => {
      const valueA = String(a[key] || '').toLowerCase();
      const valueB = String(b[key] || '').toLowerCase();
      return valueA.localeCompare(valueB);
    });
  }
}
