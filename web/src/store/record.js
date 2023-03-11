export default {
    state: {
        is_record: false,
        user1_steps: "",
        user2_steps: "",
        record_loser: "",
    },
    getters: {
    },
    mutations: {
        updateIsRecord(state, is_record) {
            state.is_record = is_record;
        },
        updateSteps(state, data) {
            state.user1_steps = data.user1_steps;
            state.user2_steps = data.user2_steps;
        },
        updateRecordLoser(state, record_loser) {
            state.record_loser = record_loser;
        },
    },
    actions: {

    },
    modules: {
    }
}