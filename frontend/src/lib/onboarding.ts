/** Key localStorage đánh dấu user đã qua onboarding. Tách ra module nhẹ để App.tsx dùng
 *  mà không phải import tĩnh cả page Onboarding (giữ nó ở chunk lazy riêng). */
export const ONBOARDING_KEY = 'ht-onboarded';
