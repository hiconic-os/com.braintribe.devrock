import '@dev.hiconic/runtime';

declare module "@dev.hiconic/hc-js-base" {
    namespace hc {
        // Actual Types
        type integer = number;
        type long = bigint;
        type double = T.Double;
        type float = T.Float;
        type decimal = T.Decimal;
        type date = globalThis.Date;
        type list<T> = T.Array<T>;
        type set<T> = T.Set<T>;
        type map<K, V> = T.Map<K, V>;

        type GenericEntity = T.com.braintribe.model.generic.GenericEntity;

        // Aggregate Types
        type Simple = boolean | string | integer | long | float | double | decimal | date;
        type Scalar = Simple | Enum<any>;
        type CollectionElement = Scalar | GenericEntity | null;
        type CollectionType = T.Map<CollectionElement, CollectionElement> | T.Set<CollectionElement> | T.Array<CollectionElement>;
        type Base = CollectionElement | CollectionType;
    }

    // Local declaration within this module for types used for property declarations 
    type integer = hc.integer;
    type long = hc.long;
    type float = hc.float;
    type double = hc.double;
    type decimal = hc.decimal;
    type date = hc.date;
    type list<T> = hc.list<T>;
    type set<T> = hc.set<T>;
    type map<K, V> = hc.map<K, V>;
    type CollectionElement = hc.CollectionElement; // used for declaring collections of any possible value
    type Base = hc.Base;

    // ************************************
    // Model Type Declaration Utility Types
    // ************************************

    type EssentialPropertyMetaData = {}

    type PropertyMeta = {
        nullable?: boolean,
        md?: EssentialPropertyMetaData[]
    }

    /** Use to declare a non-nullable property and/or additional metadata. */
    type P<T extends Base, M extends PropertyMeta = {}> = {
        type: T;
        meta: M;
    };

    type PropertyDeclarationType = Base | P<any, any>;

    type ActualPropertyType<T extends PropertyDeclarationType> = T extends P<infer U, any> ? U : T;

    /** Ensures properties are nullable by default, but can be made non-nullable with P<type, { nullable: false }> */
    type Entity<TS extends string, T extends Record<string, PropertyDeclarationType> = {}> = { TypeSignature(): TS } & {
        [K in keyof T]:
        T[K] extends P<infer U, { nullable: false }> ? U :
        // our collections are never nullable
        ActualPropertyType<T[K]> extends hc.CollectionType ? ActualPropertyType<T[K]> :
        ActualPropertyType<T[K]> | null
    };

    type Evaluable<RESULT extends Base> = {
            Eval(evaluator: hc.eval.Evaluator<GenericEntity>): hc.eval.JsEvalContext<RESULT>;
            EvalAndGet(evaluator: hc.eval.Evaluator<GenericEntity>): globalThis.Promise<RESULT>;
            EvalAndGetReasoned(evaluator: hc.eval.Evaluator<GenericEntity>): globalThis.Promise<hc.reason.Maybe<RESULT>>;
    }

    // ***********************************
    // T + hc namespaces
    // ***********************************

    namespace T {

        class Double extends Number {
            constructor(value: number);
            type(): "d";
        }

        class Float extends Number {
            constructor(value: number);
            type(): "f";
        }

        interface Array<T> {
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

            [Symbol.iterator](): IterableIterator<T, void>;
            entries(): IterableIterator<[number, T], void>;
            keys(): IterableIterator<number, void>;
            values(): IterableIterator<T, void>;

            // ###################
            //    es2016.array
            // ###################

            includes(searchElement: T, fromIndex?: number): boolean;

            // ###################
            //    es2022.array
            // ###################

            at(index: number): T | undefined;
        }

        interface Set<T> {
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

            [Symbol.iterator](): IterableIterator<T, void>;
            entries(): IterableIterator<[T, T], void>;
            keys(): IterableIterator<T, void>;
            values(): IterableIterator<T, void>;
        }

        interface Map<K, V> {
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

            [Symbol.iterator](): IterableIterator<[K, V], void>;
            entries(): IterableIterator<[K, V], void>;
            keys(): IterableIterator<K, void>;
            values(): IterableIterator<V, void>;
        }

    }

    namespace hc {
        const Symbol: {
            readonly enumType: unique symbol
        }
    }

    type PropsOnly<T> = Pick<T, { [K in keyof T]: T[K] extends Function ? never : K }[keyof T]>

    namespace hc.reflection {
        interface EntityBase {
            Properties(): readonly hc.reflection.Property[]
            // This is very tricky and really needs to be a generic function
            // If return type was simply string[], then accessing entity[propertyName] would be an error due to imlicity any
            // If return type was (keyof this)[], then sub-types would not be assinable to their supertypes due to incompatible PropertyNames()
            PropertyNames<T extends this>(): readonly (keyof PropsOnly<T>)[]
            TypeSignature(): string
        }
    }

}