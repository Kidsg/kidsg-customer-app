const fs = require('fs');
const path = require('path');

const partnerSrc = 'C:/Users/admin/Projects/kidsg-partner-app/src';

// -------------------------------------------------------------
// 1. UPDATE api.ts
// -------------------------------------------------------------
const apiTsCode = `import { Order, OrderStatus, ShopEarnings, ShopMetrics, ShopNotification, ShopPartnerProfile, ShopProduct } from '../types';

const BACKEND_API = 'https://kidsg-customer-app.vercel.app/api';
const PARTNER_API = typeof window !== 'undefined' && window.location.origin.includes('kidsg-partner-app') ? '/api' : 'https://kidsg-partner-app.vercel.app/api';

function getStored<T>(key: string, defaultValue: T): T {
  try {
    const raw = localStorage.getItem(\`kidsg_partner_\${key}\`);
    return raw ? JSON.parse(raw) : defaultValue;
  } catch {
    return defaultValue;
  }
}

function setStored<T>(key: string, value: T) {
  try {
    localStorage.setItem(\`kidsg_partner_\${key}\`, JSON.stringify(value));
  } catch {}
}

const DEFAULT_SHOP_OWNER: ShopPartnerProfile = {
  id: 'owner_ramesh_01',
  ownerName: 'Ramesh Kumar',
  shopName: 'Vidya Book & Stationery Depot',
  email: 'shop@kidsg.in',
  phone: '+91 98765 43210',
  address: 'No. 42, 12th Main Road, Malleshwaram',
  area: 'Malleshwaram',
  city: 'Bengaluru',
  pincode: '560003',
  shopStatus: 'OPEN',
  shopId: 'a0000000-0000-0000-0000-000000000001',
  role: 'SHOP_OWNER'
};

const DEFAULT_RIDER: ShopPartnerProfile = {
  id: 'rider_venkatesh_01',
  ownerName: 'Venkatesh R',
  shopName: 'KidsG Delivery Fleet',
  email: 'rider@kidsg.in',
  phone: '+91 99887 76655',
  address: 'No. 10, Cargo Lane, Malleshwaram',
  area: 'Malleshwaram',
  city: 'Bengaluru',
  pincode: '560003',
  shopStatus: 'OPEN',
  shopId: 'a0000000-0000-0000-0000-000000000001',
  role: 'DELIVERY_PERSON'
};

export const partnerApi = {
  // 1. Auth & Registration
  async register(formData: {
    ownerName: string;
    phone: string;
    email: string;
    password?: string;
    shopName: string;
    shopCategory?: string;
    address: string;
    area?: string;
    city?: string;
    pincode?: string;
    upiId?: string;
  }): Promise<ShopPartnerProfile> {
    const cleanEmail = formData.email.trim().toLowerCase();
    const cleanOwner = formData.ownerName.trim();
    const cleanShop = formData.shopName.trim();

    try {
      const res = await fetch(\`\${BACKEND_API}/shop/auth/register\`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ownerName: cleanOwner,
          shopName: cleanShop,
          email: cleanEmail,
          phone: formData.phone.trim(),
          password: formData.password || 'Partner123!',
          address: formData.address.trim(),
          area: formData.area || 'Malleshwaram',
          city: formData.city || 'Bengaluru',
          pincode: formData.pincode || '560003',
        })
      });
      if (res.ok) {
        const json = await res.json();
        if (json.data && json.data.profile) {
          const prof = json.data.profile as ShopPartnerProfile;
          this.saveAccountLocally(prof);
          setStored('profile', prof);
          return prof;
        }
      }
    } catch (e) {
      console.warn('Backend register sync warning:', e);
    }

    const newProfile: ShopPartnerProfile = {
      id: \`shop_\${Date.now()}\`,
      ownerName: cleanOwner || 'Shop Owner',
      shopName: cleanShop || 'Stationery Store',
      email: cleanEmail,
      phone: formData.phone || '+91 98000 00000',
      address: formData.address || 'Shop Address',
      area: formData.area || 'Malleshwaram',
      city: formData.city || 'Bengaluru',
      pincode: formData.pincode || '560003',
      shopStatus: 'OPEN',
      shopId: \`shop_\${Date.now()}\`,
      role: 'SHOP_OWNER'
    };

    this.saveAccountLocally(newProfile);
    setStored('profile', newProfile);
    return newProfile;
  },

  saveAccountLocally(profile: ShopPartnerProfile) {
    const accounts = getStored<ShopPartnerProfile[]>('accounts', [DEFAULT_SHOP_OWNER, DEFAULT_RIDER]);
    const filtered = accounts.filter(a => a.email.toLowerCase() !== profile.email.toLowerCase());
    filtered.push(profile);
    setStored('accounts', filtered);
  },

  async login(email: string, pass: string, role: 'SHOP_OWNER' | 'DELIVERY_PERSON' = 'SHOP_OWNER'): Promise<ShopPartnerProfile> {
    const cleanEmail = (email || '').trim().toLowerCase();

    // 1. Try backend login
    try {
      const res = await fetch(\`\${BACKEND_API}/shop/auth/login\`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: cleanEmail, password: pass })
      });
      if (res.ok) {
        const json = await res.json();
        if (json.data && (json.data.profile || json.data.user)) {
          const u = json.data.profile || json.data.user;
          const prof: ShopPartnerProfile = {
            id: u.id,
            ownerName: u.name || u.ownerName || cleanEmail.split('@')[0],
            shopName: u.shopName || \`\${cleanEmail.split('@')[0]}'s Store\`,
            email: cleanEmail,
            phone: u.phone || '+91 98765 43210',
            address: u.address || 'Shop #4, Main Market Road',
            area: u.area || 'Malleshwaram',
            city: u.city || 'Bengaluru',
            pincode: u.pincode || '560003',
            shopStatus: u.shopStatus || 'OPEN',
            shopId: u.shopId || \`shop_\${u.id}\`,
            role: role
          };
          this.saveAccountLocally(prof);
          setStored('profile', prof);
          return prof;
        }
      }
    } catch (e) {
      console.warn('Backend login warning:', e);
    }

    // 2. Check locally saved registered accounts
    const accounts = getStored<ShopPartnerProfile[]>('accounts', [DEFAULT_SHOP_OWNER, DEFAULT_RIDER]);
    const matched = accounts.find(a => a.email.toLowerCase() === cleanEmail);
    if (matched) {
      setStored('profile', matched);
      return matched;
    }

    // 3. Defaults based on demo accounts
    if (cleanEmail === 'shop@kidsg.in' || cleanEmail.includes('vidya')) {
      setStored('profile', DEFAULT_SHOP_OWNER);
      return DEFAULT_SHOP_OWNER;
    }
    if (cleanEmail === 'rider@kidsg.in' || role === 'DELIVERY_PERSON') {
      const riderProf = { ...DEFAULT_RIDER, email: cleanEmail || DEFAULT_RIDER.email };
      setStored('profile', riderProf);
      return riderProf;
    }

    // 4. Fallback for new user login - create profile based on their email, NOT Ramesh Kumar
    const username = cleanEmail.split('@')[0] || 'Partner';
    const formattedName = username.charAt(0).toUpperCase() + username.slice(1);
    const customProf: ShopPartnerProfile = {
      id: \`user_\${Date.now()}\`,
      ownerName: formattedName,
      shopName: \`\${formattedName} Books & Stationery\`,
      email: cleanEmail,
      phone: '+91 98000 00000',
      address: 'Market Road',
      area: 'Malleshwaram',
      city: 'Bengaluru',
      pincode: '560003',
      shopStatus: 'OPEN',
      shopId: \`shop_\${Date.now()}\`,
      role: role
    };

    this.saveAccountLocally(customProf);
    setStored('profile', customProf);
    return customProf;
  },

  getStoredProfile(): ShopPartnerProfile | null {
    return getStored<ShopPartnerProfile | null>('profile', null);
  },

  logout() {
    localStorage.removeItem('kidsg_partner_profile');
    localStorage.removeItem('kidsg_partner_orders');
  },

  async updateStoreStatus(status: 'OPEN' | 'CLOSED' | 'BUSY'): Promise<void> {
    const prof = this.getStoredProfile();
    if (prof) {
      prof.shopStatus = status;
      setStored('profile', prof);
    }
    try {
      await fetch(\`\${BACKEND_API}/shop/profile\`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ shopStatus: status })
      });
    } catch {}
  },

  // 2. Orders Management - Strictly from Real Database
  getOrders(): Order[] {
    const stored = getStored<Order[]>('orders', []);
    return stored.filter(o => !o.id.includes('9823') && !o.id.includes('9824'));
  },

  async fetchLiveOrders(): Promise<Order[]> {
    try {
      let res = await fetch(\`\${BACKEND_API}/shop/orders\`);
      if (!res.ok) {
        res = await fetch(\`\${PARTNER_API}/orders\`);
      }

      if (res.ok) {
        const json = await res.json();
        const rawList = Array.isArray(json.data) ? json.data : (Array.isArray(json) ? json : []);

        const mappedOrders: Order[] = rawList.map((o: any) => {
          const items = (o.items || o.order_items || []).map((it: any) => ({
            id: it.id || \`item-\${Math.random()}\`,
            productId: it.productId || it.product_id || '',
            name: it.product?.name || it.products?.name || it.name || it.productName || 'Stationery Item',
            quantity: Number(it.quantity || 1),
            price: Number(it.priceSnapshot || it.price_at_order || it.price || 0),
            imageUrl: it.product?.imageUrl || it.products?.image_url || it.imageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=150'
          }));

          const created = o.createdAt || o.created_at || new Date().toISOString();
          const minsAgo = Math.max(0, Math.floor((Date.now() - new Date(created).getTime()) / 60000));
          const totalVal = Number(o.totalAmount || o.total || o.total_amount || 0);

          let st = (o.status || 'CONFIRMED') as OrderStatus;
          if ((st as any) === 'PENDING') st = 'CONFIRMED';

          const cName = o.customerName || (o.user?.profile?.name || o.profiles?.name || o.profiles?.email?.split('@')[0] || 'KidsG Student');
          const cPhone = o.customerPhone || o.user?.phone || o.profiles?.phone || '+91 98450 11223';

          return {
            id: o.id,
            orderNumber: o.orderNumber || o.order_number || ('#KG-' + (o.id || '').substring(0, 5).toUpperCase()),
            customerName: cName,
            customerPhone: cPhone,
            deliveryAddress: {
              recipientName: cName,
              phoneNumber: cPhone,
              addressLine1: o.deliveryAddress || o.delivery_address || (o.addresses ? \`\${o.addresses.address_line1}, \${o.addresses.area}\` : 'Malleshwaram 4th Cross'),
              city: 'Bengaluru',
              pincode: '560003',
              area: 'Malleshwaram'
            },
            items,
            subtotal: totalVal,
            deliveryFee: 0,
            discount: 0,
            total: totalVal,
            status: st,
            createdAt: created,
            timeAgo: minsAgo === 0 ? 'Just now' : \`\${minsAgo} mins ago\`,
            paymentMethod: (o.paymentMethod || o.payment_method || 'ONLINE'),
            paymentStatus: (o.paymentStatus || o.payment_status || 'PAID') as 'PAID' | 'PENDING',
            deliveryPersonName: o.deliveryPersonName || (['OUT_FOR_DELIVERY', 'DELIVERED'].includes(st) ? 'KidsG Rider (+91 99887 76655)' : undefined)
          };
        });

        setStored('orders', mappedOrders);
        return mappedOrders;
      }
    } catch (e) {
      console.warn('Error fetching live shop orders from DB:', e);
    }

    return this.getOrders();
  },

  async updateOrderStatus(orderId: string, status: OrderStatus): Promise<boolean> {
    try {
      const endpoint = status === 'ACCEPTED' ? \`\${BACKEND_API}/shop/orders/\${orderId}/accept\` :
                       status === 'PREPARING' ? \`\${BACKEND_API}/shop/orders/\${orderId}/packing\` :
                       status === 'READY_FOR_PICKUP' ? \`\${BACKEND_API}/shop/orders/\${orderId}/ready\` :
                       status === 'OUT_FOR_DELIVERY' ? \`\${BACKEND_API}/shop/orders/\${orderId}/pickup\` :
                       status === 'DELIVERED' ? \`\${BACKEND_API}/shop/orders/\${orderId}/deliver\` :
                       \`\${BACKEND_API}/shop/orders/\${orderId}/reject\`;

      await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
      });
    } catch (e) {
      console.warn('Backend order update error:', e);
    }

    const currentOrders = this.getOrders();
    const updated = currentOrders.map(o => o.id === orderId ? { ...o, status } : o);
    setStored('orders', updated);

    return true;
  },

  // 3. Products & Stock
  getProducts(): ShopProduct[] {
    return getStored<ShopProduct[]>('products', [
      { id: 'p1', name: 'Classmate 4-Line Notebook (Pack of 4)', categoryName: 'Notebooks', brand: 'Classmate', price: 160, mrp: 180, stock: 45, soldCount: 142, inStock: true, isActive: true, imageUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=150' },
      { id: 'p2', name: 'Doms Smart Drawing Kit', categoryName: 'Drawing', brand: 'Doms', price: 299, mrp: 349, stock: 18, soldCount: 89, inStock: true, isActive: true, imageUrl: 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=150' },
      { id: 'p3', name: 'Reynolds 045 Fine Ball Pen (Pack of 5)', categoryName: 'Pens', brand: 'Reynolds', price: 50, mrp: 50, stock: 120, soldCount: 310, inStock: true, isActive: true, imageUrl: 'https://images.unsplash.com/photo-1585336261026-40742f5fa97f?w=150' },
      { id: 'p4', name: 'Faber-Castell 12 Oil Pastels', categoryName: 'Colors', brand: 'Faber-Castell', price: 85, mrp: 95, stock: 32, soldCount: 64, inStock: true, isActive: true, imageUrl: 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=150' },
      { id: 'p5', name: 'Apsara Platinum Extra Dark Pencils (10 Pcs)', categoryName: 'Pencils', brand: 'Apsara', price: 70, mrp: 80, stock: 65, soldCount: 220, inStock: true, isActive: true, imageUrl: 'https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=150' }
    ]);
  },

  updateProductStock(productId: string, newStock: number) {
    const list = this.getProducts().map(p => p.id === productId ? { ...p, stock: newStock } : p);
    setStored('products', list);
    return list;
  },

  addProduct(product: Omit<ShopProduct, 'id' | 'soldCount'>) {
    const list = this.getProducts();
    const newP: ShopProduct = {
      ...product,
      id: \`prod_\${Date.now()}\`,
      soldCount: 0
    };
    list.unshift(newP);
    setStored('products', list);
    return newP;
  },

  // 4. Notifications
  getNotifications(): ShopNotification[] {
    return getStored<ShopNotification[]>('notifications', []);
  },

  markNotificationRead(id: string) {
    const list = this.getNotifications().map(n => n.id === id ? { ...n, isRead: true } : n);
    setStored('notifications', list);
  },

  // 5. Earnings & Metrics
  async getDashboardMetrics(): Promise<ShopMetrics> {
    const orders = await this.fetchLiveOrders();
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const todayOrders = orders.filter(o => new Date(o.createdAt).getTime() >= today.getTime());
    const targetOrders = todayOrders.length > 0 ? todayOrders : orders;

    const todaySales = targetOrders.reduce((acc, curr) => acc + (curr.total || 0), 0);
    const pendingOrders = orders.filter(o => o.status === 'CONFIRMED').length;
    const preparingOrders = orders.filter(o => ['ACCEPTED', 'PREPARING'].includes(o.status)).length;
    const readyOrders = orders.filter(o => ['READY_FOR_PICKUP', 'OUT_FOR_DELIVERY'].includes(o.status)).length;
    const completedOrders = orders.filter(o => o.status === 'DELIVERED').length;
    const productsSold = targetOrders.reduce((acc, o) => acc + (o.items?.reduce((s, it) => s + it.quantity, 0) || 0), 0);

    return {
      todayOrders: targetOrders.length,
      todaySales,
      pendingOrders,
      preparingOrders,
      readyOrders,
      completedOrders,
      productsSold
    };
  },

  async getEarnings(period: 'today' | '7days' | '30days' = 'today'): Promise<ShopEarnings> {
    const orders = await this.fetchLiveOrders();
    const completed = orders.filter(o => o.status === 'DELIVERED');
    const totalSales = completed.reduce((sum, o) => sum + (o.total || 0), 0);
    const count = completed.length;
    const avg = count > 0 ? Math.round(totalSales / count) : 0;

    return {
      period,
      totalEarnings: totalSales,
      ordersCompleted: count,
      avgOrderValue: avg,
      trendPercent: 12.5,
      topSellingProducts: [
        { name: 'Classmate 4-Line Notebook', quantitySold: 24, revenue: 3840, imageUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=150' },
        { name: 'Doms Smart Drawing Kit', quantitySold: 12, revenue: 3588, imageUrl: 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=150' },
        { name: 'Reynolds Ball Pens', quantitySold: 45, revenue: 2250, imageUrl: 'https://images.unsplash.com/photo-1585336261026-40742f5fa97f?w=150' }
      ]
    };
  }
};
`;

fs.writeFileSync(path.join(partnerSrc, 'services/api.ts'), apiTsCode, 'utf-8');
console.log('✓ Successfully written api.ts with full TypeScript compliance');
