let waitingTasks = [];
const PermissionInfo = {
    permissions: ref({}),
    initialized: false,
    init: function (data) {
        this.initialized = true;
        this.permissions.value = data;
        if (waitingTasks !== null && waitingTasks !== undefined &&
            waitingTasks.length > 0) {
            for (const promise of waitingTasks) {
                promise.doAction();
            }
        }
        waitingTasks = null;
    },
    hasPermissionAsync: function (group, name) {
        let promiseResolve;
        const promise = new Promise(resolve => {
            promiseResolve = resolve;
        });
        promise.doAction = () => {
            promiseResolve(PermissionInfo.hasPermission(group, name));
        }
        if (PermissionInfo.initialized) promise.doAction();
        else waitingTasks.push(promise);
        return promise;
    },
    hasPermission: function (group, name) {
        const permissions1 = PermissionInfo.permissions.value[group];
        const regExp = new RegExp(name, "i");
        return permissions1 instanceof Array && Boolean(permissions1.find(item => regExp.test(item.name)));
    },
    waitingForInitialize: async function () {
        if (PermissionInfo.initialized) {
            return Promise.resolve();
        } else {
            let promiseResolve;
            const promise = new Promise(resolve => {
                promiseResolve = resolve;
            });
            promise.doAction = () => {
                promiseResolve();
            }
            waitingTasks.push(promise);
            return promise;
        }
    }
}
export default PermissionInfo;