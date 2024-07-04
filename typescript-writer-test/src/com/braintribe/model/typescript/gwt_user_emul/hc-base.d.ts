type integer = number;
type long = bigint;
type double = $T.Double;
type float = $T.Float;
type decimal = $T.Decimal;
type date = globalThis.Date;
type list<T> = $T.Array<T>;
type set<T> = $T.Set<T>;
type map<K, V> = $T.Map<K, V>;

declare namespace $T {

    class Double extends Number {
        constructor(value: number);
        type(): string; // returns 'd'
    }

    class Float extends Number {
        constructor(value: number);
        type(): string; // returns 'f'
    }

    class Array<T> {

        // ###################
        //        es5
        // ###################

        length: number;

        toString(): string;
        toLocaleString(): string;

        pop(): T | undefined;
        push(...items: T[]): number;
        concat(...items: (T | ConcatArray<T>)[]): T[];
        join(separator?: string): string;
        reverse(): T[];
        shift(): T | undefined;
        slice(start?: number, end?: number): T[];
        sort(compareFn?: (a: T, b: T) => number): this;
        splice(start: number, deleteCount?: number): T[];
        splice(start: number, deleteCount: number, ...items: T[]): T[];
        unshift(...items: T[]): number;
        indexOf(searchElement: T, fromIndex?: number): number;
        lastIndexOf(searchElement: T, fromIndex?: number): number;
        every<S extends T>(predicate: (value: T, index: number, array: T[]) => value is S, thisArg?: any): this is S[];
        every(predicate: (value: T, index: number, array: T[]) => unknown, thisArg?: any): boolean;
        some(predicate: (value: T, index: number, array: T[]) => unknown, thisArg?: any): boolean;
        forEach(callbackfn: (value: T, index: number, array: T[]) => void, thisArg?: any): void;
        map<U>(callbackfn: (value: T, index: number, array: T[]) => U, thisArg?: any): U[];
        filter<S extends T>(predicate: (value: T, index: number, array: T[]) => value is S, thisArg?: any): S[];
        filter(predicate: (value: T, index: number, array: T[]) => unknown, thisArg?: any): T[];
        reduce(callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) => T): T;
        reduce(callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) => T, initialValue: T): T;
        reduce<U>(callbackfn: (previousValue: U, currentValue: T, currentIndex: number, array: T[]) => U, initialValue: U): U;
        reduceRight(callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) => T): T;
        reduceRight(callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) => T, initialValue: T): T;
        reduceRight<U>(callbackfn: (previousValue: U, currentValue: T, currentIndex: number, array: T[]) => U, initialValue: U): U;

        // ###################
        //    es2015.core
        // ###################

        find<S extends T>(predicate: (value: T, index: number, obj: T[]) => value is S, thisArg?: any): S | undefined;
        find(predicate: (value: T, index: number, obj: T[]) => unknown, thisArg?: any): T | undefined;
        findIndex(predicate: (value: T, index: number, obj: T[]) => unknown, thisArg?: any): number;
        findLastIndex(predicate: (value: T, index: number, obj: T[]) => unknown, thisArg?: any): number;
        fill(value: T, start?: number, end?: number): this;
        copyWithin(target: number, start: number, end?: number): this;

        // ###################
        //    es2015.iterable
        // ###################

        [Symbol.iterator](): IterableIterator<T>;
        entries(): IterableIterator<[number, T]>;
        keys(): IterableIterator<number>;
        values(): IterableIterator<T>;

        // ###################
        //    es2016.array
        // ###################

        includes(searchElement: T, fromIndex?: number): boolean;

        // ###################
        //    es2022.array
        // ###################

        at(index: number): T | undefined;
   }

   class Set<T> {

        // ###################
        // es2015.collections
        // ###################

        add(value: T): this;
        clear(): void;
        delete(value: T): boolean;
        forEach(callbackfn: (value: T, value2: T, set: Set<T>) => void, thisArg?: any): void;
        has(value: T): boolean;
        readonly size: number;

        // ###################
        //   es2015.iterable
        // ###################

        [Symbol.iterator](): IterableIterator<T>;
        entries(): IterableIterator<[T, T]>;
        keys(): IterableIterator<T>;
        values(): IterableIterator<T>;

   }

   class Map<K, V> {

        // ###################
        // es2015.collections
        // ###################

        clear(): void;
        delete(key: K): boolean;
        forEach(callbackfn: (value: V, key: K, map: Map<K, V>) => void, thisArg?: any): void;
        get(key: K): V | undefined;
        has(key: K): boolean;
        set(key: K, value: V): this;
        readonly size: number;

        // ###################
        //   es2015.iterable
        // ###################

        [Symbol.iterator](): IterableIterator<[K, V]>;
        entries(): IterableIterator<[K, V]>;
        keys(): IterableIterator<K>;
        values(): IterableIterator<V>;

    }

}