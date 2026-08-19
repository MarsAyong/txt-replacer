package com.marsayong.txtreplacer

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerLibrary: Spinner
    private lateinit var containerRules: LinearLayout
    private lateinit var tvRuleCount: TextView
    private lateinit var etInput: EditText
    private lateinit var etOutput: EditText
    private lateinit var spinnerIn: Spinner
    private lateinit var spinnerOut: Spinner
    private lateinit var tvStatus: TextView

    private var libraries: MutableList<RuleLibrary> = mutableListOf()
    private var currentIndex = 0
    private var libraryAdapter: ArrayAdapter<String>? = null

    // 支持的编码
    private val encodings = arrayOf("UTF-8", "GBK", "GB2312", "Big5", "UTF-16LE", "ASCII")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerLibrary = findViewById(R.id.spinner_library)
        containerRules = findViewById(R.id.container_rules)
        tvRuleCount = findViewById(R.id.tv_rule_count)
        etInput = findViewById(R.id.et_input)
        etOutput = findViewById(R.id.et_output)
        spinnerIn = findViewById(R.id.spinner_in_encoding)
        spinnerOut = findViewById(R.id.spinner_out_encoding)
        tvStatus = findViewById(R.id.tv_status)

        // 编码下拉
        val encAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, encodings)
        encAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerIn.adapter = encAdapter
        spinnerOut.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, encodings).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerOut.setSelection(0) // 默认 UTF-8

        // 加载库
        libraries = LibraryStore.loadLibraries(this)
        refreshLibrarySpinner()

        // 按钮
        findViewById<Button>(R.id.btn_new_library).setOnClickListener { onNewLibrary() }
        findViewById<Button>(R.id.btn_rename_library).setOnClickListener { onRenameLibrary() }
        findViewById<Button>(R.id.btn_delete_library).setOnClickListener { onDeleteLibrary() }
        findViewById<Button>(R.id.btn_add_rule).setOnClickListener { addRuleRow() }
        findViewById<Button>(R.id.btn_open).setOnClickListener { openFile() }
        findViewById<Button>(R.id.btn_replace).setOnClickListener { onReplace() }
        findViewById<Button>(R.id.btn_copy).setOnClickListener { onCopy() }
        findViewById<Button>(R.id.btn_save).setOnClickListener { onSave() }

        // 让输入/结果框内部可上下滚动（单指拖动，无需依赖输入法光标翻页）
        etInput.movementMethod = ScrollingMovementMethod()
        etOutput.movementMethod = ScrollingMovementMethod()
        etInput.isVerticalScrollBarEnabled = true
        etOutput.isVerticalScrollBarEnabled = true

        // 库切换
        spinnerLibrary.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position < libraries.size) {
                    currentIndex = position
                    LibraryStore.saveCurrentIndex(this@MainActivity, position)
                    renderRules()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ============ 库管理 ============
    private fun refreshLibrarySpinner() {
        libraryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            libraries.map { it.name })
        libraryAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLibrary.adapter = libraryAdapter
        if (libraries.isNotEmpty()) {
            var idx = LibraryStore.currentIndex(this)
            if (idx !in libraries.indices) idx = 0
            spinnerLibrary.setSelection(idx)
            currentIndex = idx
        }
        renderRules()
    }

    private fun currentLibrary(): RuleLibrary? =
        if (libraries.isEmpty()) null else libraries[currentIndex]

    private fun persist() = LibraryStore.saveLibraries(this, libraries)

    private fun onNewLibrary() {
        val input = EditText(this).apply { hint = "例如：戒色1号库" }
        AlertDialog.Builder(this)
            .setTitle("新建替换库")
            .setView(input)
            .setPositiveButton("创建") { _, _ ->
                val name = input.text.toString().trim()
                libraries.add(RuleLibrary(name = if (name.isEmpty()) "未命名库" else name))
                persist()
                refreshLibrarySpinner()
                spinnerLibrary.setSelection(libraries.size - 1)
                toast("已创建库")
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun onRenameLibrary() {
        val lib = currentLibrary() ?: return
        val input = EditText(this).apply { setText(lib.name) }
        AlertDialog.Builder(this)
            .setTitle("重命名当前库")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lib.name = name
                    persist()
                    refreshLibrarySpinner()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun onDeleteLibrary() {
        if (libraries.isEmpty()) return
        val lib = currentLibrary()!!
        AlertDialog.Builder(this)
            .setTitle("删除库")
            .setMessage("确定删除「${lib.name}」及其全部规则吗？")
            .setPositiveButton("删除") { _, _ ->
                libraries.removeAt(currentIndex)
                if (currentIndex >= libraries.size) currentIndex = libraries.size - 1
                persist()
                refreshLibrarySpinner()
                toast("已删除")
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // ============ 规则行动态增删 ============
    private fun renderRules() {
        containerRules.removeAllViews()
        val lib = currentLibrary()
        if (lib == null) {
            tvRuleCount.text = "无库可用"
            return
        }
        tvRuleCount.text = "共 ${lib.rules.size} 条规则，执行时按顺序替换"
        lib.rules.forEachIndexed { i, rule ->
            containerRules.addView(buildRuleRow(i, rule))
        }
    }

    private fun buildRuleRow(index: Int, rule: Rule): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 4, 0, 4)
        }
        val etFrom = EditText(this).apply {
            setText(rule.from)
            hint = "查找词"
            isSingleLine = true
            setBackgroundResource(R.drawable.bg_input)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvArrow = TextView(this).apply {
            text = " ➜ "
            textSize = 16f
            setTextColor(0xFF94A3B8.toInt())
        }
        val etTo = EditText(this).apply {
            setText(rule.to)
            hint = "替换为"
            isSingleLine = true
            setBackgroundResource(R.drawable.bg_input)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val btnDel = Button(this).apply {
            text = "✕"
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFEF4444.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 编辑时实时写回 model
        etFrom.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (index < libraries[currentIndex].rules.size) {
                    libraries[currentIndex].rules[index] =
                        libraries[currentIndex].rules[index].copy(from = s?.toString() ?: "")
                    persist()
                }
            }
        })
        etTo.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (index < libraries[currentIndex].rules.size) {
                    libraries[currentIndex].rules[index] =
                        libraries[currentIndex].rules[index].copy(to = s?.toString() ?: "")
                    persist()
                }
            }
        })
        btnDel.setOnClickListener {
            libraries[currentIndex].rules.removeAt(index)
            persist()
            renderRules()
        }

        row.addView(etFrom)
        row.addView(tvArrow)
        row.addView(etTo)
        row.addView(btnDel)
        return row
    }

    private fun addRuleRow() {
        val lib = currentLibrary() ?: return
        lib.rules.add(Rule())
        persist()
        renderRules()
        containerRules.post { scrollToBottom() }
    }

    private fun scrollToBottom() {
        val parent = containerRules.parent as? ScrollView ?: return
        parent.post { parent.fullScroll(View.FOCUS_DOWN) }
    }

    // ============ 核心：编码转换 + 替换 ============
    private fun onReplace() {
        val lib = currentLibrary()
        if (lib == null) { toast("请先创建库"); return }
        val src = etInput.text.toString()
        if (src.isEmpty()) { toast("请先输入文本"); return }
        if (lib.rules.none { it.from.isNotEmpty() }) { toast("当前库没有替换规则"); return }

        var result = src
        lib.rules.forEach { r ->
            if (r.from.isNotEmpty()) {
                result = result.replace(r.from, r.to)
            }
        }
        etOutput.setText(result)
        tvStatus.text = "✅ 替换完成：共应用 ${lib.rules.count { it.from.isNotEmpty() }} 条规则（${lib.name}）"
    }

    private fun onCopy() {
        val out = etOutput.text.toString()
        if (out.isEmpty()) { toast("结果为空"); return }
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("result", out))
        toast("已复制")
    }

    // ============ 打开文件（SAF，按所选输入编码解码） ============
    private var openRequestCode = 1001
    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivityForResult(intent, openRequestCode)
        } catch (e: Exception) {
            toast("无法打开文件选择器")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == openRequestCode && resultCode == RESULT_OK && data?.data != null) {
            val uri = data.data!!
            try {
                contentResolver.openInputStream(uri)?.use { ins ->
                    val bytes = ins.readBytes()
                    val encoding = spinnerIn.selectedItem.toString()
                    val text = Encoder.decode(bytes, encoding)
                    etInput.setText(text)
                    tvStatus.text = "📂 已读取，按 $encoding 解码"
                }
            } catch (e: Exception) {
                toast("读取失败: ${e.message}")
            }
        } else if (requestCode == saveRequestCode && resultCode == RESULT_OK && data?.data != null) {
            val uri = data.data!!
            val out = etOutput.text.toString()
            val encoding = pendingSaveEncoding ?: "UTF-8"
            try {
                contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(Encoder.encode(out, encoding))
                    os.flush()
                }
                // 拿文件名（若有）用于提示；SAF 不暴露绝对路径，这里只给友好反馈
                var display = "已保存（$encoding）"
                try {
                    val m = contentResolver.query(
                        uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null
                    )
                    m?.use { cur ->
                        if (cur.moveToFirst()) {
                            val name = cur.getString(0)
                            display = "已保存：$name（$encoding）"
                        }
                    }
                } catch (_: Exception) {}
                tvStatus.text = "✅ $display"
                toast("保存成功（$encoding）")
            } catch (e: Exception) {
                toast("保存失败: ${e.message}")
            }
        }
    }


    // ============ 保存文件（SAF，用户自选路径，默认 Download，免 root） ============
    private var saveRequestCode = 1002
    private var pendingSaveEncoding: String? = null

    private fun onSave() {
        val out = etOutput.text.toString()
        if (out.isEmpty()) { toast("结果为空，请先执行替换"); return }

        val nameInput = EditText(this).apply {
            setText("替换结果.txt")
        }
        AlertDialog.Builder(this)
            .setTitle("保存文件名")
            .setView(nameInput)
            .setPositiveButton("下一步") { _, _ ->
                val fileName = nameInput.text.toString().trim().ifEmpty { "替换结果.txt" }
                pendingSaveEncoding = spinnerOut.selectedItem.toString()
                // 系统文件选择器：用户自己选保存位置（默认 Download 等可写目录，免 root）
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, fileName)
                    addFlags(
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                try {
                    startActivityForResult(intent, saveRequestCode)
                } catch (e: Exception) {
                    toast("无法打开保存选择器")
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
