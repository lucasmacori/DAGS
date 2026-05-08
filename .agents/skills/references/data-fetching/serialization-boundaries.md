# REA-DAT-03: Minimize Client Boundary Serialization

Only pass the fields a client component actually renders or mutates. Large objects crossing a server/client boundary increase payload size and hydration work.

## **Why This Matters:**

- **Payload Size:** Serialized props are embedded in the response and follow-up requests.
- **Clarity:** Narrow props make client components easier to reason about and test.

## **Incorrect:**

```typescript
export default async function Page() {
  const user = await fetchUser();
  return <UserBadge user={user} />;
}

'use client';

type User = {
  id: string;
  name: string;
  email: string;
  loyaltyStatus: string;
};

const UserBadge = ({ user }: { user: User }) => {
  return <span>{user.name}</span>;
};
```

## **Correct:**

```typescript
export default async function Page() {
  const user = await fetchUser();
  return <UserBadge name={user.name} />;
}

'use client';

const UserBadge = ({ name }: { name: string }) => {
  return <span>{name}</span>;
};
```

## Related Resources

- [Server Components - react.dev](https://react.dev/reference/rsc/server-components)
