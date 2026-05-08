# REA-CMP-02: Derive Values During Rendering

If a value can be computed from current props or state, calculate it during rendering. Do not mirror it into state with `useEffect()`.

## **Why This Matters:**

- **Performance:** Avoids extra renders caused by effect-driven state synchronization.
- **Reliability:** Prevents derived state from drifting out of sync with its sources.

## **Incorrect:**

```typescript
const NamePreview = () => {
  const [firstName, setFirstName] = useState('Ada');
  const [lastName, setLastName] = useState('Lovelace');
  const [fullName, setFullName] = useState('');

  useEffect(() => {
    setFullName(`${firstName} ${lastName}`);
  }, [firstName, lastName]);

  return <p>{fullName}</p>;
};
```

## **Correct:**

```typescript
const NamePreview = () => {
  const [firstName, setFirstName] = useState('Ada');
  const [lastName, setLastName] = useState('Lovelace');
  const fullName = `${firstName} ${lastName}`;

  return <p>{fullName}</p>;
};
```

## Related Resources

- [You Might Not Need an Effect - react.dev](https://react.dev/learn/you-might-not-need-an-effect)
