# REA-CMP-03: Define Components at Module Scope

Do not declare components inside other components. Inline component definitions create a new component type on every render and force remounts.

## **Why This Matters:**

- **State Preservation:** Remounting resets local state, focus, and effects.
- **Performance:** React cannot reuse the previous component instance.

## **Incorrect:**

```typescript
const UserCard = ({ name, theme }: { name: string; theme: 'light' | 'dark' }) => {
  const Avatar = () => {
    return <span className={theme === 'dark' ? 'avatar-dark' : 'avatar-light'}>{name[0]}</span>;
  };

  return <Avatar />;
};
```

## **Correct:**

```typescript
const Avatar = ({ initial, theme }: { initial: string; theme: 'light' | 'dark' }) => {
  return <span className={theme === 'dark' ? 'avatar-dark' : 'avatar-light'}>{initial}</span>;
};

const UserCard = ({ name, theme }: { name: string; theme: 'light' | 'dark' }) => {
  return <Avatar initial={name[0]} theme={theme} />;
};
```

## Related Resources

- [Preserving and Resetting State - react.dev](https://react.dev/learn/preserving-and-resetting-state)
