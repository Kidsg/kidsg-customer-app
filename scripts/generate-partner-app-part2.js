import fs from 'fs';
import path from 'path';

const partnerAppDir = 'C:/Users/admin/Projects/kidsg-partner-app';
const srcDir = path.join(partnerAppDir, 'src');
const componentsDir = path.join(srcDir, 'components');

function write(relPath, content) {
  const fullPath = path.join(partnerAppDir, relPath);
  fs.mkdirSync(path.dirname(fullPath), { recursive: true });
  fs.writeFileSync(fullPath, content.trim(), 'utf8');
  console.log(`Wrote: ${relPath}`);
}

// 6. DashboardView.tsx
write('src/components/DashboardView.tsx', `
import React from 'react';
import { ShoppingBag, IndianRupee, Clock, CheckCircle, ChevronRight, AlertCircle, ArrowUpRight, Plus, Package } from 'lucide-react';
import { Order, ShopMetrics } from '../types';

interface DashboardViewProps {
  metrics: ShopMetrics;
  recentOrders: Order[];
  onSelectOrder: (order: Order) => void;
  onViewAllOrders: () => void;
  onAddProduct: () => void;
  onGoToDelivery: () => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  metrics,
  recentOrders,
  onSelectOrder,
  onViewAllOrders,
  onAddProduct,
  onGoToDelivery,
}) => {
  return (
    <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 16 }}>
      {/* Metrics 2x2 Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <div className="card" style={{ padding: '14px', background: 'linear-gradient(135deg, #eff6ff, #ffffff)', border: '1px solid #bfdbfe' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
            <span style={{ fontSize: 12, fontWeight: 700, color: '#1e40af' }}>Today's Orders</span>
            <div style={{ background: '#dbeafe', color: '#1d4ed8', padding: 6, borderRadius: 8 }}>
              <ShoppingBag size={16} />
            </div>
          </div>
          <div style={{ fontSize: 24, fontWeight: 800, color: '#0f172a' }}>{metrics.todayOrders}</div>
          <span style={{ fontSize: 11, color: '#16a34a', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 2, marginTop: 4 }}>
            <ArrowUpRight size={13} /> +12% from yesterday
          </span>
        </div>

        <div className="card" style={{ padding: '14px', background: 'linear-gradient(135deg, #f0fdf4, #ffffff)', border: '1px solid #bbf7d0' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
            <span style={{ fontSize: 12, fontWeight: 700, color: '#166534' }}>Today's Sales</span>
            <div style={{ background: '#dcfce7', color: '#15803d', padding: 6, borderRadius: 8 }}>
              <IndianRupee size={16} />
            </div>
          </div>
          <div style={{ fontSize: 24, fontWeight: 800, color: '#0f172a' }}>₹{metrics.todaySales.toLocaleString('en-IN')}</div>
          <span style={{ fontSize: 11, color: '#64748b', fontWeight: 500, marginTop: 4, display: 'block' }}>
            {metrics.productsSold} items dispatched
          </span>
        </div>

        <div className="card" style={{ padding: '14px', background: '#fffbeb', border: '1px solid #fde68a' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
            <span style={{ fontSize: 12, fontWeight: 700, color: '#92400e' }}>Pending Confirmation</span>
            <div style={{ background: '#fef3c7', color: '#b45309', padding: 6, borderRadius: 8 }}>
              <AlertCircle size={16} />
            </div>
          </div>
          <div style={{ fontSize: 24, fontWeight: 800, color: '#b45309' }}>{metrics.pendingOrders}</div>
          <span style={{ fontSize: 11, color: '#b45309', fontWeight: 600, marginTop: 4, display: 'block' }}>
            Requires immediate attention
          </span>
        </div>

        <div className="card" style={{ padding: '14px', background: '#f5f3ff', border: '1px solid #ddd6fe' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
            <span style={{ fontSize: 12, fontWeight: 700, color: '#5b21b6' }}>Ready for Pickup</span>
            <div style={{ background: '#ede9fe', color: '#6d28d9', padding: 6, borderRadius: 8 }}>
              <Clock size={16} />
            </div>
          </div>
          <div style={{ fontSize: 24, fontWeight: 800, color: '#6d28d9' }}>{metrics.readyOrders}</div>
          <span style={{ fontSize: 11, color: '#6d28d9', fontWeight: 600, marginTop: 4, display: 'block' }}>
            Awaiting rider pickup
          </span>
        </div>
      </div>

      {/* Quick Action Bar */}
      <div style={{ display: 'flex', gap: 10 }}>
        <button 
          onClick={onAddProduct}
          className="btn-secondary"
          style={{ flex: 1, padding: '10px 12px', fontSize: 13, justifyContent: 'center' }}
        >
          <Plus size={16} /> Add Product
        </button>
        <button 
          onClick={onGoToDelivery}
          className="btn-secondary"
          style={{ flex: 1, padding: '10px 12px', fontSize: 13, justifyContent: 'center' }}
        >
          <Package size={16} /> Delivery Fleet
        </button>
      </div>

      {/* Recent Orders Section */}
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <h3 style={{ fontSize: 16, fontWeight: 800, color: '#0f172a' }}>Recent Orders</h3>
          <button 
            onClick={onViewAllOrders}
            style={{ background: 'none', border: 'none', color: '#2563eb', fontSize: 12, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer' }}
          >
            View All ({recentOrders.length}) <ChevronRight size={14} />
          </button>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {recentOrders.slice(0, 4).map(order => (
            <div 
              key={order.id} 
              className="card"
              onClick={() => onSelectOrder(order)}
              style={{ cursor: 'pointer', padding: '12px 14px', transition: 'transform 0.15s ease' }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 6 }}>
                <div>
                  <span style={{ fontSize: 13, fontWeight: 800, color: '#0f172a' }}>{order.orderNumber}</span>
                  <div style={{ fontSize: 12, color: '#64748b' }}>{order.customerName} • {order.items.length} items</div>
                </div>
                <span className={\`badge badge-\${order.status.toLowerCase().replace(/_/g, '-')}\`}>
                  {order.status.replace(/_/g, ' ')}
                </span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid #f1f5f9', paddingTop: 8, marginTop: 6 }}>
                <span style={{ fontSize: 11, color: '#94a3b8' }}>{order.timeAgo || 'Just now'}</span>
                <span style={{ fontSize: 14, fontWeight: 800, color: '#0f172a' }}>₹{order.total}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
`);

// 7. OrdersView.tsx
write('src/components/OrdersView.tsx', `
import React, { useState } from 'react';
import { Search, ChevronRight, Check, Play, PackageCheck } from 'lucide-react';
import { Order, OrderStatus } from '../types';

interface OrdersViewProps {
  orders: Order[];
  onSelectOrder: (order: Order) => void;
  onUpdateStatus: (orderId: string, status: OrderStatus) => void;
}

export const OrdersView: React.FC<OrdersViewProps> = ({
  orders,
  onSelectOrder,
  onUpdateStatus,
}) => {
  const [filter, setFilter] = useState<'ALL' | 'NEW' | 'ACCEPTED' | 'PREPARING' | 'READY_FOR_PICKUP' | 'OUT_FOR_DELIVERY' | 'DELIVERED'>('ALL');
  const [search, setSearch] = useState('');

  const filterTabs = [
    { id: 'ALL', label: 'All' },
    { id: 'NEW', label: 'New / Pending' },
    { id: 'ACCEPTED', label: 'Accepted' },
    { id: 'PREPARING', label: 'Preparing' },
    { id: 'READY_FOR_PICKUP', label: 'Ready for Pickup' },
    { id: 'OUT_FOR_DELIVERY', label: 'Out for Delivery' },
    { id: 'DELIVERED', label: 'Delivered' }
  ];

  const filteredOrders = orders.filter(o => {
    if (filter === 'NEW' && o.status !== 'CONFIRMED' && o.status !== 'PENDING_PAYMENT') return false;
    if (filter === 'ACCEPTED' && o.status !== 'ACCEPTED') return false;
    if (filter === 'PREPARING' && o.status !== 'PREPARING') return false;
    if (filter === 'READY_FOR_PICKUP' && o.status !== 'READY_FOR_PICKUP') return false;
    if (filter === 'OUT_FOR_DELIVERY' && o.status !== 'OUT_FOR_DELIVERY') return false;
    if (filter === 'DELIVERED' && o.status !== 'DELIVERED') return false;

    if (search.trim()) {
      const q = search.toLowerCase();
      return (
        o.orderNumber.toLowerCase().includes(q) ||
        o.customerName.toLowerCase().includes(q) ||
        o.items.some(i => i.name.toLowerCase().includes(q))
      );
    }
    return true;
  });

  return (
    <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 14 }}>
      {/* Search Input */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, background: 'white', border: '1px solid #cbd5e1', borderRadius: 12, padding: '8px 12px' }}>
        <Search size={16} color="#94a3b8" />
        <input
          type="text"
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="Search by order # or customer..."
          style={{ border: 'none', outline: 'none', width: '100%', fontSize: 13 }}
        />
      </div>

      {/* Horizontal Filter Pills */}
      <div style={{ display: 'flex', gap: 8, overflowX: 'auto', paddingBottom: 4 }}>
        {filterTabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => setFilter(tab.id as any)}
            style={{
              padding: '6px 14px',
              borderRadius: 999,
              fontSize: 12,
              fontWeight: 700,
              whiteSpace: 'nowrap',
              border: filter === tab.id ? '1px solid #2563eb' : '1px solid #e2e8f0',
              background: filter === tab.id ? '#eff6ff' : 'white',
              color: filter === tab.id ? '#2563eb' : '#64748b',
              cursor: 'pointer'
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Orders List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {filteredOrders.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 20px', color: '#94a3b8' }}>
            <p style={{ fontSize: 14, fontWeight: 600 }}>No orders found in this status.</p>
          </div>
        ) : (
          filteredOrders.map(order => (
            <div key={order.id} className="card" style={{ padding: '14px' }}>
              <div 
                onClick={() => onSelectOrder(order)}
                style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}
              >
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ fontSize: 14, fontWeight: 800, color: '#0f172a' }}>{order.orderNumber}</span>
                    <span style={{ fontSize: 11, color: '#94a3b8' }}>• {order.timeAgo}</span>
                  </div>
                  <div style={{ fontSize: 12, fontWeight: 600, color: '#475569', marginTop: 2 }}>{order.customerName}</div>
                  <div style={{ fontSize: 11, color: '#64748b' }}>{order.deliveryAddress.area}, {order.deliveryAddress.city}</div>
                </div>
                <span className={\`badge badge-\${order.status.toLowerCase().replace(/_/g, '-')}\`}>
                  {order.status.replace(/_/g, ' ')}
                </span>
              </div>

              {/* Items Preview */}
              <div style={{ background: '#f8fafc', padding: '8px 10px', borderRadius: 8, margin: '8px 0', fontSize: 12, color: '#475569' }}>
                {order.items.map(item => (
                  <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '2px 0' }}>
                    <span>{item.quantity}x {item.name}</span>
                    <span style={{ fontWeight: 600 }}>₹{item.price * item.quantity}</span>
                  </div>
                ))}
              </div>

              {/* Total and Action buttons */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 8 }}>
                <div>
                  <span style={{ fontSize: 11, color: '#94a3b8' }}>Total Amount</span>
                  <div style={{ fontSize: 16, fontWeight: 800, color: '#0f172a' }}>₹{order.total}</div>
                </div>

                <div style={{ display: 'flex', gap: 8 }}>
                  {order.status === 'CONFIRMED' && (
                    <button
                      className="btn-success"
                      onClick={() => onUpdateStatus(order.id, 'ACCEPTED')}
                      style={{ padding: '8px 14px', fontSize: 12, width: 'auto' }}
                    >
                      <Check size={14} /> Accept Order
                    </button>
                  )}

                  {order.status === 'ACCEPTED' && (
                    <button
                      className="btn-primary"
                      onClick={() => onUpdateStatus(order.id, 'PREPARING')}
                      style={{ padding: '8px 14px', fontSize: 12, width: 'auto' }}
                    >
                      <Play size={14} /> Start Packing
                    </button>
                  )}

                  {order.status === 'PREPARING' && (
                    <button
                      className="btn-primary"
                      onClick={() => onUpdateStatus(order.id, 'READY_FOR_PICKUP')}
                      style={{ padding: '8px 14px', fontSize: 12, width: 'auto', background: '#7c3aed' }}
                    >
                      <PackageCheck size={14} /> Ready for Pickup
                    </button>
                  )}

                  <button
                    className="btn-secondary"
                    onClick={() => onSelectOrder(order)}
                    style={{ padding: '8px 12px', fontSize: 12 }}
                  >
                    Details <ChevronRight size={14} />
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
`);

// 8. OrderDetailView.tsx
write('src/components/OrderDetailView.tsx', `
import React from 'react';
import { ArrowLeft, Phone, MapPin, CheckCircle, Clock, Package, Bike, User, ShieldCheck } from 'lucide-react';
import { Order, OrderStatus } from '../types';

interface OrderDetailViewProps {
  order: Order;
  onBack: () => void;
  onUpdateStatus: (orderId: string, status: OrderStatus) => void;
  onAssignRider: (orderId: string) => void;
}

export const OrderDetailView: React.FC<OrderDetailViewProps> = ({
  order,
  onBack,
  onUpdateStatus,
  onAssignRider,
}) => {
  const steps: { key: OrderStatus; label: string }[] = [
    { key: 'CONFIRMED', label: 'Order Placed' },
    { key: 'ACCEPTED', label: 'Accepted' },
    { key: 'PREPARING', label: 'Preparing' },
    { key: 'READY_FOR_PICKUP', label: 'Ready for Pickup' },
    { key: 'OUT_FOR_DELIVERY', label: 'Out for Delivery' },
    { key: 'DELIVERED', label: 'Delivered' }
  ];

  const currentStepIdx = steps.findIndex(s => s.key === order.status);

  return (
    <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 16 }}>
      {/* Top Bar */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <button 
          onClick={onBack}
          style={{ background: 'none', border: 'none', display: 'flex', alignItems: 'center', gap: 6, color: '#64748b', fontSize: 13, fontWeight: 700, cursor: 'pointer' }}
        >
          <ArrowLeft size={16} /> Back to Orders
        </button>
        <span className={\`badge badge-\${order.status.toLowerCase().replace(/_/g, '-')}\`}>
          {order.status.replace(/_/g, ' ')}
        </span>
      </div>

      <div>
        <h2 style={{ fontSize: 20, fontWeight: 800, color: '#0f172a' }}>{order.orderNumber}</h2>
        <span style={{ fontSize: 12, color: '#64748b' }}>Placed {order.timeAgo} • Payment {order.paymentStatus} ({order.paymentMethod})</span>
      </div>

      {/* Stepper Progress */}
      <div className="card" style={{ padding: '14px' }}>
        <div style={{ fontSize: 12, fontWeight: 700, color: '#334155', marginBottom: 12 }}>Fulfillment Timeline</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {steps.map((st, idx) => {
            const isCompleted = idx <= (currentStepIdx === -1 ? 0 : currentStepIdx);
            const isCurrent = idx === currentStepIdx;

            return (
              <div key={st.key} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div style={{
                  width: 24,
                  height: 24,
                  borderRadius: '50%',
                  background: isCompleted ? '#2563eb' : '#e2e8f0',
                  color: isCompleted ? 'white' : '#94a3b8',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: 11,
                  fontWeight: 700
                }}>
                  {isCompleted ? <CheckCircle size={14} /> : idx + 1}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 13, fontWeight: isCurrent ? 800 : 600, color: isCompleted ? '#0f172a' : '#94a3b8' }}>
                    {st.label}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Customer & Address Card */}
      <div className="card" style={{ padding: '14px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <div style={{ background: '#eff6ff', color: '#2563eb', padding: 8, borderRadius: 10 }}>
              <User size={18} />
            </div>
            <div>
              <div style={{ fontSize: 14, fontWeight: 800, color: '#0f172a' }}>{order.customerName}</div>
              <div style={{ fontSize: 12, color: '#64748b' }}>{order.customerPhone}</div>
            </div>
          </div>

          <a 
            href={\`tel:\${order.customerPhone}\`}
            style={{ display: 'flex', alignItems: 'center', gap: 4, background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '6px 12px', borderRadius: 8, fontSize: 12, fontWeight: 700, textDecoration: 'none' }}
          >
            <Phone size={14} /> Call
          </a>
        </div>

        <div style={{ display: 'flex', alignItems: 'flex-start', gap: 8, borderTop: '1px solid #f1f5f9', paddingTop: 10, marginTop: 4 }}>
          <MapPin size={16} color="#64748b" style={{ flexShrink: 0, marginTop: 2 }} />
          <div style={{ fontSize: 12, color: '#475569', lineHeight: 1.4 }}>
            <strong>{order.deliveryAddress.recipientName}</strong><br />
            {order.deliveryAddress.addressLine1}, {order.deliveryAddress.addressLine2}<br />
            {order.deliveryAddress.area}, {order.deliveryAddress.city} - {order.deliveryAddress.pincode}
          </div>
        </div>
      </div>

      {/* Ordered Items List */}
      <div className="card" style={{ padding: '14px' }}>
        <div style={{ fontSize: 13, fontWeight: 800, color: '#0f172a', marginBottom: 10 }}>
          Items in this Order ({order.items.length})
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {order.items.map(item => (
            <div key={item.id} style={{ display: 'flex', alignItems: 'center', gap: 10, paddingBottom: 8, borderBottom: '1px solid #f1f5f9' }}>
              <img 
                src={item.imageUrl} 
                alt={item.name} 
                style={{ width: 44, height: 44, borderRadius: 8, objectFit: 'cover', background: '#f1f5f9' }} 
              />
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: '#0f172a' }}>{item.name}</div>
                <div style={{ fontSize: 12, color: '#64748b' }}>Qty: {item.quantity} × ₹{item.price}</div>
              </div>
              <div style={{ fontSize: 14, fontWeight: 800, color: '#0f172a' }}>
                ₹{item.price * item.quantity}
              </div>
            </div>
          ))}
        </div>

        {/* Bill Summary */}
        <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 6, fontSize: 12, color: '#64748b' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>Item Subtotal</span>
            <span style={{ fontWeight: 600, color: '#0f172a' }}>₹{order.subtotal}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>Delivery Fee</span>
            <span style={{ fontWeight: 600, color: '#0f172a' }}>₹{order.deliveryFee}</span>
          </div>
          {order.discount > 0 && (
            <div style={{ display: 'flex', justifyContent: 'space-between', color: '#16a34a' }}>
              <span>Discount</span>
              <span>-₹{order.discount}</span>
            </div>
          )}
          <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid #e2e8f0', paddingTop: 8, marginTop: 4, fontSize: 15, fontWeight: 800, color: '#0f172a' }}>
            <span>Total Bill</span>
            <span>₹{order.total}</span>
          </div>
        </div>
      </div>

      {/* Action Footer */}
      <div style={{ marginTop: 8, display: 'flex', flexDirection: 'column', gap: 10 }}>
        {order.status === 'CONFIRMED' && (
          <div style={{ display: 'flex', gap: 10 }}>
            <button
              className="btn-danger-outline"
              onClick={() => onUpdateStatus(order.id, 'CANCELLED')}
              style={{ flex: 1 }}
            >
              Reject
            </button>
            <button
              className="btn-success"
              onClick={() => onUpdateStatus(order.id, 'ACCEPTED')}
              style={{ flex: 2 }}
            >
              <CheckCircle size={18} /> Accept Order
            </button>
          </div>
        )}

        {order.status === 'ACCEPTED' && (
          <button
            className="btn-primary"
            onClick={() => onUpdateStatus(order.id, 'PREPARING')}
          >
            <Package size={18} /> Start Preparing & Packing
          </button>
        )}

        {order.status === 'PREPARING' && (
          <button
            className="btn-primary"
            onClick={() => onUpdateStatus(order.id, 'READY_FOR_PICKUP')}
            style={{ background: '#7c3aed' }}
          >
            <CheckCircle size={18} /> Mark as Ready for Pickup
          </button>
        )}

        {order.status === 'READY_FOR_PICKUP' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div style={{ background: '#f5f3ff', border: '1px solid #ddd6fe', padding: '10px 14px', borderRadius: 10, fontSize: 12, color: '#5b21b6', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Clock size={16} /> Ready in store. Delivery riders can pick up this order.
            </div>
            <button
              className="btn-primary"
              onClick={() => onAssignRider(order.id)}
            >
              <Bike size={18} /> Assign / Notify KidsG Rider
            </button>
          </div>
        )}

        {order.status === 'OUT_FOR_DELIVERY' && (
          <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', padding: '12px 14px', borderRadius: 12 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#15803d', fontWeight: 700, fontSize: 13, marginBottom: 4 }}>
              <Bike size={18} /> Rider On The Way
            </div>
            <p style={{ fontSize: 12, color: '#166534' }}>
              Rider {order.deliveryPersonName || 'Venkatesh R'} picked up the package and is delivering to the customer.
            </p>
          </div>
        )}

        {order.status === 'DELIVERED' && (
          <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', padding: '12px 14px', borderRadius: 12, display: 'flex', alignItems: 'center', gap: 10 }}>
            <ShieldCheck size={22} color="#16a34a" />
            <div>
              <div style={{ fontSize: 13, fontWeight: 700, color: '#15803d' }}>Order Delivered Successfully</div>
              <div style={{ fontSize: 11, color: '#166534' }}>Payment has been recorded to daily earnings.</div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
`);

// 9. DeliveryView.tsx
write('src/components/DeliveryView.tsx', `
import React, { useState } from 'react';
import { Bike, MapPin, Store, Navigation, CheckCircle, Clock, ShieldCheck, ChevronRight } from 'lucide-react';
import { Order, OrderStatus } from '../types';

interface DeliveryViewProps {
  orders: Order[];
  onAcceptPickup: (orderId: string) => void;
  onMarkPickedUp: (orderId: string) => void;
  onStartDelivery: (orderId: string) => void;
  onMarkDelivered: (orderId: string) => void;
}

export const DeliveryView: React.FC<DeliveryViewProps> = ({
  orders,
  onAcceptPickup,
  onMarkPickedUp,
  onStartDelivery,
  onMarkDelivered,
}) => {
  const [subTab, setSubTab] = useState<'AVAILABLE' | 'ACTIVE' | 'COMPLETED'>('AVAILABLE');

  const availableOrders = orders.filter(o => o.status === 'READY_FOR_PICKUP');
  const activeOrders = orders.filter(o => o.status === 'OUT_FOR_DELIVERY');
  const completedOrders = orders.filter(o => o.status === 'DELIVERED');

  return (
    <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 14 }}>
      {/* Rider Status Header */}
      <div className="card" style={{ padding: '12px 16px', background: 'linear-gradient(135deg, #0f172a, #1e293b)', color: 'white' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{ background: '#2563eb', padding: 8, borderRadius: 10, color: 'white' }}>
              <Bike size={20} />
            </div>
            <div>
              <div style={{ fontSize: 14, fontWeight: 800 }}>KidsG Delivery Fleet</div>
              <div style={{ fontSize: 11, color: '#94a3b8' }}>Rider: Venkatesh R (Online)</div>
            </div>
          </div>
          <span style={{ background: '#16a34a', color: 'white', fontSize: 11, fontWeight: 700, padding: '3px 10px', borderRadius: 999 }}>
            ON DUTY
          </span>
        </div>
      </div>

      {/* 3 Subtabs */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', background: '#f1f5f9', padding: 4, borderRadius: 12 }}>
        <button
          onClick={() => setSubTab('AVAILABLE')}
          style={{
            padding: '8px 4px',
            border: 'none',
            borderRadius: 8,
            fontSize: 12,
            fontWeight: 700,
            cursor: 'pointer',
            background: subTab === 'AVAILABLE' ? 'white' : 'transparent',
            color: subTab === 'AVAILABLE' ? '#2563eb' : '#64748b',
            boxShadow: subTab === 'AVAILABLE' ? '0 1px 3px rgba(0,0,0,0.08)' : 'none'
          }}
        >
          Available ({availableOrders.length})
        </button>
        <button
          onClick={() => setSubTab('ACTIVE')}
          style={{
            padding: '8px 4px',
            border: 'none',
            borderRadius: 8,
            fontSize: 12,
            fontWeight: 700,
            cursor: 'pointer',
            background: subTab === 'ACTIVE' ? 'white' : 'transparent',
            color: subTab === 'ACTIVE' ? '#2563eb' : '#64748b',
            boxShadow: subTab === 'ACTIVE' ? '0 1px 3px rgba(0,0,0,0.08)' : 'none'
          }}
        >
          Active ({activeOrders.length})
        </button>
        <button
          onClick={() => setSubTab('COMPLETED')}
          style={{
            padding: '8px 4px',
            border: 'none',
            borderRadius: 8,
            fontSize: 12,
            fontWeight: 700,
            cursor: 'pointer',
            background: subTab === 'COMPLETED' ? 'white' : 'transparent',
            color: subTab === 'COMPLETED' ? '#2563eb' : '#64748b',
            boxShadow: subTab === 'COMPLETED' ? '0 1px 3px rgba(0,0,0,0.08)' : 'none'
          }}
        >
          Completed ({completedOrders.length})
        </button>
      </div>

      {/* Available Pickups */}
      {subTab === 'AVAILABLE' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {availableOrders.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px 20px', color: '#94a3b8' }}>
              <Clock size={32} style={{ margin: '0 auto 8px', opacity: 0.5 }} />
              <p style={{ fontSize: 14, fontWeight: 600 }}>No orders currently ready for pickup.</p>
              <p style={{ fontSize: 12 }}>Stores will notify you when packaging is completed.</p>
            </div>
          ) : (
            availableOrders.map(order => (
              <div key={order.id} className="card" style={{ padding: '14px', borderLeft: '4px solid #7c3aed' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                  <div>
                    <span style={{ fontSize: 14, fontWeight: 800, color: '#0f172a' }}>{order.orderNumber}</span>
                    <div style={{ fontSize: 12, color: '#64748b' }}>{order.items.length} items • ₹{order.total}</div>
                  </div>
                  <span style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '3px 8px', borderRadius: 6, fontSize: 11, fontWeight: 700 }}>
                    Earn ₹45
                  </span>
                </div>

                {/* Pickup and Delivery details */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 12, color: '#475569', margin: '8px 0' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <Store size={14} color="#2563eb" />
                    <span><strong>Pickup:</strong> Vidya Stationery (1.2 km away)</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <MapPin size={14} color="#ea580c" />
                    <span><strong>Drop:</strong> {order.deliveryAddress.area}, {order.deliveryAddress.city}</span>
                  </div>
                </div>

                <button
                  className="btn-primary"
                  onClick={() => onAcceptPickup(order.id)}
                  style={{ marginTop: 6 }}
                >
                  <Bike size={16} /> Accept Delivery Request
                </button>
              </div>
            ))
          )}
        </div>
      )}

      {/* Active Deliveries */}
      {subTab === 'ACTIVE' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          {activeOrders.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px 20px', color: '#94a3b8' }}>
              <p style={{ fontSize: 14, fontWeight: 600 }}>No active deliveries right now.</p>
              <p style={{ fontSize: 12 }}>Switch to Available tab to accept delivery orders.</p>
            </div>
          ) : (
            activeOrders.map(order => (
              <div key={order.id} className="card" style={{ padding: '16px', border: '2px solid #2563eb' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 }}>
                  <div>
                    <span style={{ fontSize: 15, fontWeight: 800, color: '#0f172a' }}>{order.orderNumber}</span>
                    <div style={{ fontSize: 12, color: '#64748b' }}>Customer: {order.customerName}</div>
                  </div>
                  <span className="badge badge-delivery">Out for Delivery</span>
                </div>

                {/* Stepper for Rider Actions */}
                <div style={{ background: '#f8fafc', padding: '12px', borderRadius: 12, margin: '8px 0' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8, fontSize: 12, fontWeight: 700, color: '#1e293b' }}>
                    <Navigation size={16} color="#2563eb" /> Drop-off Destination:
                  </div>
                  <div style={{ fontSize: 12, color: '#475569', lineHeight: 1.4, paddingLeft: 24 }}>
                    {order.deliveryAddress.addressLine1}, {order.deliveryAddress.area}<br />
                    Phone: <strong>{order.customerPhone}</strong>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
                  <button
                    className="btn-success"
                    onClick={() => onMarkDelivered(order.id)}
                  >
                    <CheckCircle size={16} /> Mark as Delivered (Complete)
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Completed History */}
      {subTab === 'COMPLETED' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#f0fdf4', padding: '10px 14px', borderRadius: 10, border: '1px solid #bbf7d0' }}>
            <span style={{ fontSize: 12, fontWeight: 700, color: '#166534' }}>Today's Rider Earnings:</span>
            <span style={{ fontSize: 15, fontWeight: 800, color: '#15803d' }}>₹{completedOrders.length * 45}</span>
          </div>

          {completedOrders.map(order => (
            <div key={order.id} className="card" style={{ padding: '12px 14px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontSize: 13, fontWeight: 700, color: '#0f172a' }}>{order.orderNumber}</div>
                  <div style={{ fontSize: 11, color: '#64748b' }}>{order.deliveryAddress.area} • Delivered {order.timeAgo}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: 13, fontWeight: 800, color: '#16a34a' }}>+₹45</div>
                  <span style={{ fontSize: 10, color: '#94a3b8' }}>Paid</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
`);

// 10. ProductsView.tsx
write('src/components/ProductsView.tsx', `
import React, { useState } from 'react';
import { Search, Plus, Minus, AlertTriangle, Check, Package, X } from 'lucide-react';
import { ShopProduct } from '../types';

interface ProductsViewProps {
  products: ShopProduct[];
  onUpdateStock: (productId: string, newStock: number, isActive?: boolean) => void;
  onAddProduct: (p: Omit<ShopProduct, 'id' | 'soldCount'>) => void;
}

export const ProductsView: React.FC<ProductsViewProps> = ({
  products,
  onUpdateStock,
  onAddProduct,
}) => {
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState<'ALL' | 'IN_STOCK' | 'LOW_STOCK'>('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);

  // New product form state
  const [newName, setNewName] = useState('');
  const [newCategory, setNewCategory] = useState('Notebooks');
  const [newBrand, setNewBrand] = useState('Classmate');
  const [newPrice, setNewPrice] = useState('60');
  const [newMrp, setNewMrp] = useState('70');
  const [newStock, setNewStock] = useState('25');
  const [newImage, setNewImage] = useState('https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=200');

  const filtered = products.filter(p => {
    if (filter === 'IN_STOCK' && p.stock <= 5) return false;
    if (filter === 'LOW_STOCK' && p.stock > 5) return false;
    if (search.trim()) {
      const q = search.toLowerCase();
      return p.name.toLowerCase().includes(q) || p.brand.toLowerCase().includes(q) || p.categoryName.toLowerCase().includes(q);
    }
    return true;
  });

  const handleCreateProduct = (e: React.FormEvent) => {
    e.preventDefault();
    onAddProduct({
      name: newName,
      categoryName: newCategory,
      brand: newBrand,
      price: Number(newPrice),
      mrp: Number(newMrp),
      stock: Number(newStock),
      isActive: true,
      imageUrl: newImage
    });
    setIsModalOpen(false);
    setNewName('');
  };

  return (
    <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 14 }}>
      {/* Top Action & Search */}
      <div style={{ display: 'flex', gap: 10 }}>
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: 8, background: 'white', border: '1px solid #cbd5e1', borderRadius: 12, padding: '8px 12px' }}>
          <Search size={16} color="#94a3b8" />
          <input
            type="text"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search products or brand..."
            style={{ border: 'none', outline: 'none', width: '100%', fontSize: 13 }}
          />
        </div>

        <button
          className="btn-primary"
          onClick={() => setIsModalOpen(true)}
          style={{ width: 'auto', padding: '8px 14px', borderRadius: 12, fontSize: 13 }}
        >
          <Plus size={16} /> Add Item
        </button>
      </div>

      {/* Stock Filter Chips */}
      <div style={{ display: 'flex', gap: 8 }}>
        <button
          onClick={() => setFilter('ALL')}
          style={{
            padding: '6px 14px',
            borderRadius: 999,
            fontSize: 12,
            fontWeight: 700,
            border: filter === 'ALL' ? '1px solid #2563eb' : '1px solid #e2e8f0',
            background: filter === 'ALL' ? '#eff6ff' : 'white',
            color: filter === 'ALL' ? '#2563eb' : '#64748b',
            cursor: 'pointer'
          }}
        >
          All Items ({products.length})
        </button>

        <button
          onClick={() => setFilter('LOW_STOCK')}
          style={{
            padding: '6px 14px',
            borderRadius: 999,
            fontSize: 12,
            fontWeight: 700,
            border: filter === 'LOW_STOCK' ? '1px solid #d97706' : '1px solid #e2e8f0',
            background: filter === 'LOW_STOCK' ? '#fffbeb' : 'white',
            color: filter === 'LOW_STOCK' ? '#d97706' : '#64748b',
            cursor: 'pointer'
          }}
        >
          Low Stock (&lt; 5)
        </button>
      </div>

      {/* Products List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {filtered.map(product => {
          const isLowStock = product.stock <= 5;

          return (
            <div key={product.id} className="card" style={{ padding: '12px', display: 'flex', gap: 12, alignItems: 'center' }}>
              <img
                src={product.imageUrl}
                alt={product.name}
                style={{ width: 60, height: 60, borderRadius: 10, objectFit: 'cover', background: '#f1f5f9' }}
              />

              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 11, color: '#2563eb', fontWeight: 700, textTransform: 'uppercase' }}>
                  {product.categoryName} • {product.brand}
                </div>
                <div style={{ fontSize: 13, fontWeight: 700, color: '#0f172a', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {product.name}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 2 }}>
                  <span style={{ fontSize: 14, fontWeight: 800, color: '#0f172a' }}>₹{product.price}</span>
                  {product.mrp > product.price && (
                    <span style={{ fontSize: 11, color: '#94a3b8', textDecoration: 'line-through' }}>₹{product.mrp}</span>
                  )}
                </div>
              </div>

              {/* Stock Stepper */}
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4 }}>
                <span style={{
                  fontSize: 11,
                  fontWeight: 700,
                  color: isLowStock ? '#d97706' : '#16a34a',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 3
                }}>
                  {isLowStock && <AlertTriangle size={12} />}
                  {product.stock} in stock
                </span>

                <div style={{ display: 'flex', alignItems: 'center', gap: 4, background: '#f1f5f9', borderRadius: 8, padding: 2 }}>
                  <button
                    onClick={() => onUpdateStock(product.id, product.stock - 1)}
                    style={{ border: 'none', background: 'white', width: 24, height: 24, borderRadius: 6, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                  >
                    <Minus size={12} />
                  </button>
                  <span style={{ width: 20, textAlign: 'center', fontSize: 12, fontWeight: 700 }}>{product.stock}</span>
                  <button
                    onClick={() => onUpdateStock(product.id, product.stock + 1)}
                    style={{ border: 'none', background: 'white', width: 24, height: 24, borderRadius: 6, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                  >
                    <Plus size={12} />
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Add Product Modal */}
      {isModalOpen && (
        <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
          <div className="modal-sheet" onClick={e => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h3 style={{ fontSize: 18, fontWeight: 800 }}>Add New Stationery Item</h3>
              <button onClick={() => setIsModalOpen(false)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleCreateProduct} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Item Name *</label>
                <input
                  type="text"
                  value={newName}
                  onChange={e => setNewName(e.target.value)}
                  placeholder="e.g. Apsara Drawing Book 40 Pages"
                  required
                  style={{ width: '100%', padding: '10px', borderRadius: 8, border: '1px solid #cbd5e1' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
                <div>
                  <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Brand</label>
                  <input
                    type="text"
                    value={newBrand}
                    onChange={e => setNewBrand(e.target.value)}
                    style={{ width: '100%', padding: '10px', borderRadius: 8, border: '1px solid #cbd5e1' }}
                  />
                </div>
                <div>
                  <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Category</label>
                  <select
                    value={newCategory}
                    onChange={e => setNewCategory(e.target.value)}
                    style={{ width: '100%', padding: '10px', borderRadius: 8, border: '1px solid #cbd5e1', background: 'white' }}
                  >
                    <option>Notebooks</option>
                    <option>Geometry & Math</option>
                    <option>Pencils & Pens</option>
                    <option>Art & Craft</option>
                    <option>Adhesives & Tape</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10 }}>
                <div>
                  <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Selling Price (₹)</label>
                  <input
                    type="number"
                    value={newPrice}
                    onChange={e => setNewPrice(e.target.value)}
                    required
                    style={{ width: '100%', padding: '10px', borderRadius: 8, border: '1px solid #cbd5e1' }}
                  />
                </div>
                <div>
                  <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>MRP (₹)</label>
                  <input
                    type="number"
                    value={newMrp}
                    onChange={e => setNewMrp(e.target.value)}
                    required
                    style={{ width: '100%', padding: '10px', borderRadius: 8, border: '1px solid #cbd5e1' }}
                  />
                </div>
                <div>
                  <label style={{ fontSize: 12, fontWeight: 700, display: 'block', marginBottom: 4 }}>Initial Stock</label>
                  <input
                    type="number"
                    value={newStock}
                    onChange={e => setNewStock(e.target.value)}
                    required
                    style={{ width: '100%', padding: '10px', borderRadius: 8, border: '1px solid #cbd5e1' }}
                  />
                </div>
              </div>

              <button type="submit" className="btn-primary" style={{ marginTop: 10 }}>
                Add to Store Inventory
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
`);

// 11. EarningsView.tsx
write('src/components/EarningsView.tsx', `
import React, { useState, useEffect } from 'react';
import { IndianRupee, ArrowUpRight, TrendingUp, Calendar, CreditCard, Sparkles } from 'lucide-react';
import { ShopEarnings } from '../types';
import { partnerApi } from '../services/api';

export const EarningsView: React.FC = () => {
  const [period, setPeriod] = useState<'today' | '7days' | '30days'>('today');
  const [data, setData] = useState<ShopEarnings | null>(null);

  useEffect(() => {
    partnerApi.getEarnings(period).then(setData);
  }, [period]);

  return (
    <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 16 }}>
      {/* Period Selector Tabs */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', background: '#f1f5f9', padding: 4, borderRadius: 12 }}>
        <button
          onClick={() => setPeriod('today')}
          style={{
            padding: '8px',
            border: 'none',
            borderRadius: 8,
            fontSize: 12,
            fontWeight: 700,
            cursor: 'pointer',
            background: period === 'today' ? 'white' : 'transparent',
            color: period === 'today' ? '#2563eb' : '#64748b',
            boxShadow: period === 'today' ? '0 1px 3px rgba(0,0,0,0.08)' : 'none'
          }}
        >
          Today
        </button>
        <button
          onClick={() => setPeriod('7days')}
          style={{
            padding: '8px',
            border: 'none',
            borderRadius: 8,
            fontSize: 12,
            fontWeight: 700,
            cursor: 'pointer',
            background: period === '7days' ? 'white' : 'transparent',
            color: period === '7days' ? '#2563eb' : '#64748b',
            boxShadow: period === '7days' ? '0 1px 3px rgba(0,0,0,0.08)' : 'none'
          }}
        >
          Last 7 Days
        </button>
        <button
          onClick={() => setPeriod('30days')}
          style={{
            padding: '8px',
            border: 'none',
            borderRadius: 8,
            fontSize: 12,
            fontWeight: 700,
            cursor: 'pointer',
            background: period === '30days' ? 'white' : 'transparent',
            color: period === '30days' ? '#2563eb' : '#64748b',
            boxShadow: period === '30days' ? '0 1px 3px rgba(0,0,0,0.08)' : 'none'
          }}
        >
          Last 30 Days
        </button>
      </div>

      {/* Main Earnings Card */}
      <div className="card" style={{ padding: '20px', background: 'linear-gradient(135deg, #1e3a8a, #2563eb)', color: 'white' }}>
        <span style={{ fontSize: 13, fontWeight: 600, color: '#bfdbfe' }}>Net Payout Revenue</span>
        <div style={{ fontSize: 34, fontWeight: 800, marginTop: 4, letterSpacing: -0.5 }}>
          ₹{(data?.totalEarnings || 0).toLocaleString('en-IN')}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 12, background: 'rgba(255,255,255,0.15)', padding: '6px 12px', borderRadius: 8, width: 'fit-content', fontSize: 12, fontWeight: 700 }}>
          <ArrowUpRight size={16} /> +{data?.trendPercent || 18.4}% vs previous period
        </div>
      </div>

      {/* 2 Column Stats */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <div className="card" style={{ padding: '14px' }}>
          <span style={{ fontSize: 11, fontWeight: 700, color: '#64748b' }}>COMPLETED ORDERS</span>
          <div style={{ fontSize: 20, fontWeight: 800, color: '#0f172a', marginTop: 4 }}>
            {data?.ordersCompleted || 0}
          </div>
        </div>

        <div className="card" style={{ padding: '14px' }}>
          <span style={{ fontSize: 11, fontWeight: 700, color: '#64748b' }}>AVG ORDER VALUE</span>
          <div style={{ fontSize: 20, fontWeight: 800, color: '#0f172a', marginTop: 4 }}>
            ₹{data?.avgOrderValue || 0}
          </div>
        </div>
      </div>

      {/* Top Selling Products */}
      <div className="card" style={{ padding: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 14, fontWeight: 800, color: '#0f172a', marginBottom: 12 }}>
          <Sparkles size={16} color="#d97706" /> Top Selling Items
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {data?.topSellingProducts.map((p, idx) => (
            <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: 8, borderBottom: '1px solid #f1f5f9' }}>
              <div>
                <div style={{ fontSize: 13, fontWeight: 700, color: '#0f172a' }}>{p.name}</div>
                <div style={{ fontSize: 11, color: '#64748b' }}>{p.quantitySold} units sold</div>
              </div>
              <div style={{ fontSize: 14, fontWeight: 800, color: '#16a34a' }}>
                ₹{p.revenue.toLocaleString('en-IN')}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Direct Settlement Info */}
      <div className="card" style={{ padding: '14px', background: '#f8fafc', border: '1px solid #e2e8f0', display: 'flex', alignItems: 'center', gap: 12 }}>
        <CreditCard size={24} color="#2563eb" />
        <div>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#0f172a' }}>Daily Auto Payouts Active</div>
          <div style={{ fontSize: 11, color: '#64748b' }}>Direct deposit to: <strong>ramesh@okhdfcbank</strong></div>
        </div>
      </div>
    </div>
  );
};
`);

// 12. NotificationsView.tsx
write('src/components/NotificationsView.tsx', `
import React, { useState } from 'react';
import { ArrowLeft, Bell, ShoppingBag, AlertTriangle, Info, Check } from 'lucide-react';
import { ShopNotification } from '../types';

interface NotificationsViewProps {
  notifications: ShopNotification[];
  onBack: () => void;
  onSelectNotification: (n: ShopNotification) => void;
}

export const NotificationsView: React.FC<NotificationsViewProps> = ({
  notifications,
  onBack,
  onSelectNotification,
}) => {
  const [filter, setFilter] = useState<'ALL' | 'ORDERS' | 'STOCK' | 'SYSTEM'>('ALL');

  const filtered = notifications.filter(n => {
    if (filter === 'ORDERS' && n.type !== 'ORDER' && n.type !== 'NEW_ORDER') return false;
    if (filter === 'STOCK' && n.type !== 'STOCK') return false;
    if (filter === 'SYSTEM' && n.type !== 'SYSTEM') return false;
    return true;
  });

  return (
    <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: 14 }}>
      <button 
        onClick={onBack}
        style={{ background: 'none', border: 'none', display: 'flex', alignItems: 'center', gap: 6, color: '#64748b', fontSize: 13, fontWeight: 700, cursor: 'pointer' }}
      >
        <ArrowLeft size={16} /> Back
      </button>

      <div>
        <h2 style={{ fontSize: 20, fontWeight: 800, color: '#0f172a' }}>Notifications</h2>
        <span style={{ fontSize: 12, color: '#64748b' }}>Real-time alerts on orders, stock & payouts</span>
      </div>

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: 8 }}>
        {['ALL', 'ORDERS', 'STOCK', 'SYSTEM'].map(f => (
          <button
            key={f}
            onClick={() => setFilter(f as any)}
            style={{
              padding: '6px 14px',
              borderRadius: 999,
              fontSize: 12,
              fontWeight: 700,
              border: filter === f ? '1px solid #2563eb' : '1px solid #e2e8f0',
              background: filter === f ? '#eff6ff' : 'white',
              color: filter === f ? '#2563eb' : '#64748b',
              cursor: 'pointer'
            }}
          >
            {f}
          </button>
        ))}
      </div>

      {/* Notifications List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {filtered.map(n => (
          <div
            key={n.id}
            onClick={() => onSelectNotification(n)}
            className="card"
            style={{
              padding: '12px 14px',
              cursor: 'pointer',
              background: n.isRead ? 'white' : '#f0f9ff',
              border: n.isRead ? '1px solid #e2e8f0' : '1px solid #bae6fd'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: 10 }}>
              <div style={{
                background: n.type === 'NEW_ORDER' ? '#dcfce7' : n.type === 'STOCK' ? '#fef3c7' : '#eff6ff',
                color: n.type === 'NEW_ORDER' ? '#15803d' : n.type === 'STOCK' ? '#d97706' : '#2563eb',
                padding: 8,
                borderRadius: 10
              }}>
                {n.type === 'NEW_ORDER' || n.type === 'ORDER' ? <ShoppingBag size={16} /> : n.type === 'STOCK' ? <AlertTriangle size={16} /> : <Info size={16} />}
              </div>

              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <span style={{ fontSize: 13, fontWeight: 700, color: '#0f172a' }}>{n.title}</span>
                  <span style={{ fontSize: 11, color: '#94a3b8' }}>{n.createdAt}</span>
                </div>
                <p style={{ fontSize: 12, color: '#475569', marginTop: 2 }}>{n.message}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
`);

// 13. App.tsx
write('src/App.tsx', `
import React, { useState, useEffect } from 'react';
import { Header } from './components/Header';
import { BottomNav } from './components/BottomNav';
import { WelcomeView } from './components/WelcomeView';
import { LoginView } from './components/LoginView';
import { RegisterView } from './components/RegisterView';
import { DashboardView } from './components/DashboardView';
import { OrdersView } from './components/OrdersView';
import { OrderDetailView } from './components/OrderDetailView';
import { DeliveryView } from './components/DeliveryView';
import { ProductsView } from './components/ProductsView';
import { EarningsView } from './components/EarningsView';
import { NotificationsView } from './components/NotificationsView';
import { Order, OrderStatus, ShopMetrics, ShopNotification, ShopPartnerProfile, ShopProduct } from './types';
import { partnerApi } from './services/api';

export function App() {
  const [profile, setProfile] = useState<ShopPartnerProfile | null>(partnerApi.getStoredProfile());
  const [authScreen, setAuthScreen] = useState<'welcome' | 'login' | 'register'>('welcome');
  const [activeTab, setActiveTab] = useState<'dashboard' | 'orders' | 'products' | 'delivery' | 'earnings'>('dashboard');
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const [isFullWidth, setIsFullWidth] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Data states
  const [orders, setOrders] = useState<Order[]>(partnerApi.getOrders());
  const [metrics, setMetrics] = useState<ShopMetrics>({
    todayOrders: 5,
    todaySales: 1661,
    pendingOrders: 1,
    preparingOrders: 1,
    readyOrders: 1,
    completedOrders: 1,
    productsSold: 14
  });
  const [products, setProducts] = useState<ShopProduct[]>(partnerApi.getProducts());
  const [notifications, setNotifications] = useState<ShopNotification[]>(partnerApi.getNotifications());

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  const refreshData = async () => {
    const liveOrders = await partnerApi.fetchLiveOrders();
    setOrders(liveOrders);
    const m = await partnerApi.getDashboardMetrics();
    setMetrics(m);
    setProducts(partnerApi.getProducts());
    setNotifications(partnerApi.getNotifications());
  };

  useEffect(() => {
    refreshData();
    const interval = setInterval(refreshData, 10000);
    return () => clearInterval(interval);
  }, []);

  const handleLogin = async (email: string, pass: string, role: 'SHOP_OWNER' | 'DELIVERY_PERSON') => {
    const p = await partnerApi.login(email, pass, role);
    setProfile(p);
    showToast(\`Welcome back, \${p.ownerName}!\`);
  };

  const handleToggleStoreStatus = async () => {
    if (!profile) return;
    const nextStatus = profile.shopStatus === 'OPEN' ? 'BUSY' : profile.shopStatus === 'BUSY' ? 'CLOSED' : 'OPEN';
    await partnerApi.updateStoreStatus(nextStatus);
    setProfile({ ...profile, shopStatus: nextStatus });
    showToast(\`Store status changed to \${nextStatus}\`);
  };

  const handleSwitchRole = () => {
    if (!profile) return;
    const newRole = profile.role === 'SHOP_OWNER' ? 'DELIVERY_PERSON' : 'SHOP_OWNER';
    const newProf: ShopPartnerProfile = {
      ...profile,
      role: newRole,
      ownerName: newRole === 'SHOP_OWNER' ? 'Ramesh Kumar' : 'Venkatesh R',
      shopName: newRole === 'SHOP_OWNER' ? 'Vidya Book & Stationery Depot' : 'KidsG Delivery Fleet'
    };
    setProfile(newProf);
    if (newRole === 'DELIVERY_PERSON') {
      setActiveTab('delivery');
      showToast('Switched to Delivery Partner Mode');
    } else {
      setActiveTab('dashboard');
      showToast('Switched to Shop Owner Mode');
    }
  };

  const handleUpdateOrderStatus = async (orderId: string, status: OrderStatus) => {
    await partnerApi.updateOrderStatus(orderId, status);
    await refreshData();
    if (selectedOrder && selectedOrder.id === orderId) {
      setSelectedOrder(prev => prev ? { ...prev, status } : null);
    }
    showToast(\`Order \${status.replace(/_/g, ' ')}\`);
  };

  const handleAssignRider = async (orderId: string) => {
    await partnerApi.updateOrderStatus(orderId, 'OUT_FOR_DELIVERY');
    await refreshData();
    if (selectedOrder && selectedOrder.id === orderId) {
      setSelectedOrder(prev => prev ? { ...prev, status: 'OUT_FOR_DELIVERY', deliveryPersonName: 'Venkatesh R (+91 98450 12345)' } : null);
    }
    showToast('Delivery Partner Venkatesh R assigned & en route');
  };

  const handleUpdateStock = async (productId: string, stock: number) => {
    await partnerApi.updateProductStock(productId, stock);
    setProducts(partnerApi.getProducts());
    showToast('Stock quantity updated');
  };

  const handleAddProduct = async (p: Omit<ShopProduct, 'id' | 'soldCount'>) => {
    await partnerApi.addProduct(p);
    setProducts(partnerApi.getProducts());
    showToast('New product added to store');
  };

  const unreadNotifs = notifications.filter(n => !n.isRead).length;
  const pendingOrdersCount = orders.filter(o => o.status === 'CONFIRMED').length;
  const availablePickupsCount = orders.filter(o => o.status === 'READY_FOR_PICKUP').length;

  return (
    <div className="app-viewport-wrapper">
      <div className="viewport-controls">
        <span>KidsG Partner Mobile App</span>
        <button className="viewport-toggle-btn" onClick={() => setIsFullWidth(!isFullWidth)}>
          {isFullWidth ? '📱 Switch to Phone Frame' : '💻 Switch to Full Screen'}
        </button>
      </div>

      <div className={\`phone-frame \${isFullWidth ? 'full-width' : ''}\`}>
        {/* Phone Top Notch & Status Bar */}
        <div className="phone-status-bar">
          <span>9:41</span>
          <div className="dynamic-island" />
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <span>5G</span>
            <span>100%</span>
          </div>
        </div>

        {/* Action Toast Feedback */}
        {toastMessage && (
          <div className="toast-banner">
            <span>✨</span>
            <span>{toastMessage}</span>
          </div>
        )}

        {/* Logged In Header */}
        {profile && !selectedOrder && !isNotificationsOpen && (
          <Header
            profile={profile}
            unreadCount={unreadNotifs}
            onOpenNotifications={() => setIsNotificationsOpen(true)}
            onToggleStatus={handleToggleStoreStatus}
            isFullWidth={isFullWidth}
            onToggleWidth={() => setIsFullWidth(!isFullWidth)}
            onSwitchRole={handleSwitchRole}
          />
        )}

        {/* Scrollable View Area */}
        <div className={\`screen-scroll-container \${!profile ? 'no-nav' : ''}\`}>
          {!profile ? (
            authScreen === 'welcome' ? (
              <WelcomeView
                onGetStarted={() => setAuthScreen('register')}
                onLogin={() => setAuthScreen('login')}
                onQuickLogin={(role) => handleLogin(role === 'SHOP_OWNER' ? 'shop@kidsg.in' : 'rider@kidsg.in', 'pass', role)}
              />
            ) : authScreen === 'login' ? (
              <LoginView
                onBack={() => setAuthScreen('welcome')}
                onSuccess={(email, pass, role) => handleLogin(email, pass, role)}
                onGoToRegister={() => setAuthScreen('register')}
              />
            ) : (
              <RegisterView
                onBack={() => setAuthScreen('login')}
                onSuccess={() => handleLogin('shop@kidsg.in', 'Partner123!', 'SHOP_OWNER')}
              />
            )
          ) : isNotificationsOpen ? (
            <NotificationsView
              notifications={notifications}
              onBack={() => setIsNotificationsOpen(false)}
              onSelectNotification={(n) => {
                partnerApi.markNotificationRead(n.id);
                setNotifications(partnerApi.getNotifications());
                if (n.orderId) {
                  const target = orders.find(o => o.id === n.orderId);
                  if (target) {
                    setSelectedOrder(target);
                    setIsNotificationsOpen(false);
                  }
                }
              }}
            />
          ) : selectedOrder ? (
            <OrderDetailView
              order={selectedOrder}
              onBack={() => setSelectedOrder(null)}
              onUpdateStatus={handleUpdateOrderStatus}
              onAssignRider={handleAssignRider}
            />
          ) : activeTab === 'dashboard' ? (
            <DashboardView
              metrics={metrics}
              recentOrders={orders}
              onSelectOrder={setSelectedOrder}
              onViewAllOrders={() => setActiveTab('orders')}
              onAddProduct={() => setActiveTab('products')}
              onGoToDelivery={() => setActiveTab('delivery')}
            />
          ) : activeTab === 'orders' ? (
            <OrdersView
              orders={orders}
              onSelectOrder={setSelectedOrder}
              onUpdateStatus={handleUpdateOrderStatus}
            />
          ) : activeTab === 'products' ? (
            <ProductsView
              products={products}
              onUpdateStock={handleUpdateStock}
              onAddProduct={handleAddProduct}
            />
          ) : activeTab === 'delivery' ? (
            <DeliveryView
              orders={orders}
              onAcceptPickup={handleAssignRider}
              onMarkPickedUp={(id) => handleUpdateOrderStatus(id, 'OUT_FOR_DELIVERY')}
              onStartDelivery={(id) => handleUpdateOrderStatus(id, 'OUT_FOR_DELIVERY')}
              onMarkDelivered={(id) => handleUpdateOrderStatus(id, 'DELIVERED')}
            />
          ) : (
            <EarningsView />
          )}
        </div>

        {/* Bottom Navigation */}
        {profile && !selectedOrder && !isNotificationsOpen && (
          <BottomNav
            activeTab={activeTab}
            onChangeTab={setActiveTab}
            pendingOrdersCount={pendingOrdersCount}
            availablePickupsCount={availablePickupsCount}
          />
        )}
      </div>
    </div>
  );
}

export default App;
`);

console.log('All remaining views and App.tsx created.');
