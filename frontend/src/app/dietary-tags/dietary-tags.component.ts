import {Component, Input} from '@angular/core';

interface TagInfo {
  color: string;
  icon: string;
  description: string;
}

type DietaryTagsMap = {
  [key: string]: TagInfo;
}

@Component({
  selector: 'app-dietary-tags',
  templateUrl: './dietary-tags.component.html',
  styleUrl: './dietary-tags.component.scss'
})
export class DietaryTagsComponent {
  @Input() tags: string[] = [];

  // Common dietary tags and their descriptions
  readonly DIETARY_TAGS: DietaryTagsMap = {
    'vegan': {
      color: '#4CAF50',
      icon: 'bi-flower1',
      description: 'Contains no animal products'
    },
    'vegetarian': {
      color: '#8BC34A',
      icon: 'bi-flower2',
      description: 'Contains no meat or fish, may contain eggs and dairy'
    },
    'gluten-free': {
      color: '#FFC107',
      icon: 'bi-slash-circle',
      description: 'Contains no wheat or gluten'
    },
    'dairy-free': {
      color: '#03A9F4',
      icon: 'bi-cup',
      description: 'Contains no dairy products'
    },
    'nut-free': {
      color: '#FF9800',
      icon: 'bi-x-circle',
      description: 'Contains no nuts'
    },
    'keto': {
      color: '#9C27B0',
      icon: 'bi-egg-fried',
      description: 'Low-carb, high-fat diet compatible'
    },
    'paleo': {
      color: '#795548',
      icon: 'bi-diamond',
      description: 'Based on foods similar to those eaten during the Paleolithic era'
    },
    'low-carb': {
      color: '#607D8B',
      icon: 'bi-graph-down',
      description: 'Contains minimal carbohydrates'
    }
  };

  getTagColor(tag: string): string {
    const normalizedTag = tag.toLowerCase();
    return this.DIETARY_TAGS[normalizedTag]?.color || '#9E9E9E';
  }

  getTagIcon(tag: string): string {
    const normalizedTag = tag.toLowerCase();
    return this.DIETARY_TAGS[normalizedTag]?.icon || 'bi-tag';
  }

  getTagDescription(tag: string): string {
    const normalizedTag = tag.toLowerCase();
    return this.DIETARY_TAGS[normalizedTag]?.description || '';
  }
}

