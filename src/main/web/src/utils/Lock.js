const locks = {};

/*
class ExecutablePromiseWrapper {
    promise;
    constructor(executor) {
        let resolveFunc
        const promise = new Promise((resolve) => {
            resolveFunc = resolve;
        });
        promise.resolve = resolveFunc;
        promise.execute = () => {
            promise.resolve(executor());
        }
        this.promise = promise;
    }
}
*/

const LockUtil = {
    synchronizeExecute: (lockName,executablePromise) => {
        if (locks[lockName] instanceof Array) {
            locks[lockName].push(executablePromise);
        } else {
            locks[lockName] = [];
            executablePromise.then(() => {
                let index = 0;
                const doLoop = () => {
                    const nextPromise = locks[lockName][index];
                    if (nextPromise) {
                        nextPromise.then(() => {
                            index++;
                            doLoop();
                        });
                        nextPromise.execute(nextPromise.resolve,nextPromise.reject);
                    } else {
                        locks[lockName] = undefined;
                    }
                }
                doLoop();
            });
            executablePromise.execute(executablePromise.resolve,executablePromise.reject);
        }
    },
    buildExecutablePromise: (executeFunc) => {
        let resolveFunc;
        let rejectFunc;
        const promise = new Promise((resolve, reject) => {
            resolveFunc = resolve;
            rejectFunc = reject;
        });
        promise.resolve = resolveFunc;
        promise.reject = rejectFunc;
        promise.execute = executeFunc;
        return promise;
    }
};
export default LockUtil;