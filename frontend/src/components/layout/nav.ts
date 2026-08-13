import { CalendarCheck, BarChart3, Plus, ListChecks, User } from 'lucide-react';

export interface NavItem {
  to: string;
  label: string;
  icon: typeof CalendarCheck;
  /** Center "add" action on the bottom nav triggers the create modal instead of routing. */
  action?: 'create';
}

export const NAV_ITEMS: NavItem[] = [
  { to: '/', label: 'Hôm nay', icon: CalendarCheck },
  { to: '/insights', label: 'Thống kê', icon: BarChart3 },
  { to: '/new', label: 'Thêm mới', icon: Plus, action: 'create' },
  { to: '/habits', label: 'Habits', icon: ListChecks },
  { to: '/profile', label: 'Hồ sơ', icon: User },
];
