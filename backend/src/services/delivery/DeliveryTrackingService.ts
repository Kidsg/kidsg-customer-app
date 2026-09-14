export interface DeliveryStatusStep {
  status: string;
  title: string;
  description: string;
  timestamp: string;
  completed: boolean;
}

export interface OrderTrackingInfo {
  orderId: string;
  orderStatus: string;
  deliveryStatus: string;
  riderName?: string;
  riderPhone?: string;
  storeName: string;
  storeAddress: string;
  estimatedDeliveryTime: string;
  statusHistory: DeliveryStatusStep[];
}

export interface DeliveryTrackingService {
  getTracking(orderId: string, createdAt?: string, status?: string): Promise<OrderTrackingInfo>;
}

export class MockDeliveryTrackingService implements DeliveryTrackingService {
  async getTracking(orderId: string, createdAt = new Date().toISOString(), status = 'CONFIRMED'): Promise<OrderTrackingInfo> {
    const orderDate = new Date(createdAt);
    const eta = new Date(orderDate.getTime() + 15 * 60 * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    // Deterministic progression steps based on order status
    const steps: DeliveryStatusStep[] = [
      {
        status: 'CONFIRMED',
        title: 'Order Confirmed',
        description: 'Your stationery items are confirmed by the store',
        timestamp: orderDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        completed: true,
      },
      {
        status: 'PREPARING',
        title: 'Preparing',
        description: 'Partner stationery store is carefully packing your supplies',
        timestamp: new Date(orderDate.getTime() + 3 * 60 * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        completed: ['PREPARING', 'READY_FOR_PICKUP', 'PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED'].includes(status),
      },
      {
        status: 'READY_FOR_PICKUP',
        title: 'Ready for Pickup',
        description: 'Package ready at store counter',
        timestamp: new Date(orderDate.getTime() + 6 * 60 * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        completed: ['READY_FOR_PICKUP', 'PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED'].includes(status),
      },
      {
        status: 'PICKED_UP',
        title: 'Picked Up',
        description: 'Delivery partner has collected the school stationery bag',
        timestamp: new Date(orderDate.getTime() + 8 * 60 * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        completed: ['PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED'].includes(status),
      },
      {
        status: 'OUT_FOR_DELIVERY',
        title: 'Out for Delivery',
        description: 'Speeding towards your address for tomorrow\'s classes!',
        timestamp: new Date(orderDate.getTime() + 11 * 60 * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        completed: ['OUT_FOR_DELIVERY', 'DELIVERED'].includes(status),
      },
      {
        status: 'DELIVERED',
        title: 'Delivered',
        description: 'Handed over safely. All set for school!',
        timestamp: new Date(orderDate.getTime() + 15 * 60 * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        completed: status === 'DELIVERED',
      },
    ];

    return {
      orderId,
      orderStatus: status,
      deliveryStatus: status === 'DELIVERED' ? 'DELIVERED' : 'IN_TRANSIT',
      riderName: 'Ramesh Kumar (KidsG Partner)',
      riderPhone: '+91 98765 43210',
      storeName: 'Vidya Stationery Depot',
      storeAddress: '12th Main, 4th Block, Koramangala',
      estimatedDeliveryTime: eta,
      statusHistory: steps,
    };
  }
}

export function getDeliveryTrackingService(): DeliveryTrackingService {
  return new MockDeliveryTrackingService();
}
