<template>
  <div class="app-container">
    <el-form :inline="true" size="small">
      <el-form-item><el-button type="success" @click="openAdd" icon="el-icon-plus">新增网格</el-button></el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="!dirtyCount" :loading="savingOrder" @click="saveOrder" icon="el-icon-sort">
          保存排序<span v-if="dirtyCount">（{{ dirtyCount }} 项待保存）</span>
        </el-button>
      </el-form-item>
      <el-form-item><el-button size="small" @click="resetOrder" :disabled="!dirtyCount">撤销未保存</el-button></el-form-item>
      <el-form-item><el-button size="small" @click="autoReorder">按当前顺序重编号(10,20,30...)</el-button></el-form-item>
    </el-form>

    <el-alert type="info" show-icon :closable="false" style="margin-bottom:12px"
      title="排序说明：数字越小越靠前。修改排序号后点“保存排序”一次性提交；同一网格下同排序号时按创建顺序。"/>

    <el-table v-loading="loading" :data="list" border stripe row-key="courtyardId">
      <el-table-column label="排序" width="140" align="center">
        <template slot-scope="s">
          <el-input-number size="mini" v-model="s.row.sortOrder" :min="0" :max="99999" controls-position="right"
                           @change="markDirty(s.row)" style="width:110px"/>
        </template>
      </el-table-column>
      <el-table-column label="网格名称" prop="courtyardName" min-width="180">
        <template slot-scope="s">
          <span>{{ s.row.courtyardName }}</span>
          <el-tag v-if="isDirty(s.row)" size="mini" type="warning" style="margin-left:8px">未保存</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="所属网格" prop="gridName" width="160"/>
      <el-table-column label="状态" width="100">
        <template slot-scope="s"><el-tag size="small" :type="s.row.status==='0'?'success':'danger'">{{ s.row.status==='0'?'正常':'停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template slot-scope="s">
          <el-button size="mini" @click="openEdit(s.row)">修改</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(s.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="isEdit?'修改网格':'新增网格'" :visible.sync="dialogVisible" width="400px">
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="网格名称"><el-input v-model="form.courtyardName"/></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0"/></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option label="正常" value="0"/><el-option label="停用" value="1"/></el-select></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'InspectCourtyard',
  data() {
    return {
      loading: false,
      list: [],
      originalOrderMap: {},
      savingOrder: false,
      dialogVisible: false,
      isEdit: false,
      form: { courtyardId: null, courtyardName: '', sortOrder: 0, status: '0' }
    }
  },
  computed: {
    dirtyCount() {
      return this.list.filter(r => this.isDirty(r)).length;
    }
  },
  created() { this.handleQuery(); },
  methods: {
    handleQuery() {
      this.loading = true;
      request({ url:'/inspect/courtyard/list', method:'get' }).then(res => {
        this.list = res.data || [];
        this.originalOrderMap = {};
        this.list.forEach(r => { this.originalOrderMap[r.courtyardId] = r.sortOrder; });
        this.loading = false;
      });
    },
    isDirty(row) {
      return this.originalOrderMap[row.courtyardId] !== row.sortOrder;
    },
    markDirty() { /* v-model 已同步；此处仅作为钩子，未来可以做校验 */ },
    resetOrder() {
      this.list.forEach(r => { r.sortOrder = this.originalOrderMap[r.courtyardId]; });
    },
    autoReorder() {
      this.$confirm('将按当前列表顺序重编号为 10、20、30…（便于插入新项），是否继续？', '提示', { type: 'warning' })
        .then(() => {
          this.list.forEach((r, idx) => { r.sortOrder = (idx + 1) * 10; });
          this.$message.success('已生成新排序号，记得点“保存排序”提交');
        }).catch(() => {});
    },
    saveOrder() {
      const dirty = this.list.filter(r => this.isDirty(r));
      if (!dirty.length) return;
      this.savingOrder = true;
      const tasks = dirty.map(r => request({ url:'/inspect/courtyard', method:'put', data: { ...r } }));
      Promise.all(tasks).then(() => {
        this.$message.success(`已保存 ${dirty.length} 项排序`);
        this.handleQuery();
      }).catch(() => {
        this.$message.error('部分排序保存失败，请重试');
      }).finally(() => { this.savingOrder = false; });
    },
    openAdd() { this.isEdit = false; this.form = { courtyardId: null, courtyardName: '', sortOrder: (this.list.length+1)*10, status: '0' }; this.dialogVisible = true; },
    openEdit(row) { this.isEdit = true; this.form = { ...row }; this.dialogVisible = true; },
    submitForm() {
      const fn = this.isEdit
        ? () => request({ url:'/inspect/courtyard', method:'put', data:this.form })
        : () => request({ url:'/inspect/courtyard', method:'post', data:this.form });
      fn().then(() => { this.dialogVisible = false; this.$message.success('操作成功'); this.handleQuery(); });
    },
    handleDelete(row) {
      this.$confirm('确定删除该网格?', '提示', { type: 'warning' }).then(() => {
        request({ url:'/inspect/courtyard/'+row.courtyardId, method:'delete' }).then(() => {
          this.$message.success('删除成功'); this.handleQuery();
        });
      });
    }
  }
}
</script>
