import sanitizeHtml from "sanitize-html";
import * as csstree from 'css-tree';

/**
 * 严格过滤 Mermaid 样式：只保留包含 #mm ID 选择器的规则和 @keyframes
 * @param {string} cssText - 原始 CSS 文本
 * @returns {string} 过滤后的 CSS
 */
function filterMermaidStyle(cssText) {
    let ast;
    try {
        ast = csstree.parse(cssText, { silent: true });
    } catch (e) {
        // 解析失败则返回空
        return '';
    }

    /**
     * 递归检查规则是否符合保留条件
     * @param {Object} rule - CSS 规则对象
     * @returns {boolean} 是否保留
     */
    function shouldKeepRule(rule) {
        // keyframes 直接保留
        if (rule.type === 'keyframes') return true;

        // 普通样式规则：必须至少有一个选择器包含 #mm ID
        if (rule.type === 'rule') {
            const selectors = rule.selectors || [];
            return selectors.some(sel => /#mm[a-zA-Z0-9_-]/.test(sel));
        }

        // 其他 at-rule（如 media、supports 等）：递归检查内部规则
        if (rule.type === 'media' || rule.type === 'supports' || rule.type === 'document') {
            if (rule.rules && rule.rules.some(shouldKeepRule)) {
                return true;
            }
        }

        // 其余规则（@import, @charset, @page 等）不保留
        return false;
    }

    /**
     * 递归过滤规则列表，返回新规则列表
     * @param {Array} rules
     * @returns {Array}
     */
    function filterRules(rules) {
        const result = [];
        for (const rule of rules) {
            if (shouldKeepRule(rule)) {
                // 对于 at-rule，需要深层过滤其内部规则
                if ((rule.type === 'media' || rule.type === 'supports' || rule.type === 'document') && rule.rules) {
                    const filteredChildren = filterRules(rule.rules);
                    if (filteredChildren.length > 0) {
                        // 保留外层 at-rule，并用过滤后的内部规则替换
                        const newRule = { ...rule, rules: filteredChildren };
                        result.push(newRule);
                    }
                } else {
                    // 普通规则或 keyframes 直接保留
                    result.push(rule);
                }
            }
        }
        return result;
    }

    const filteredRules = filterRules(ast.stylesheet.rules);
    if (filteredRules.length === 0) return '';

    const newAst = {
        type: 'stylesheet',
        stylesheet: { rules: filteredRules }
    };
    return csstree.generate(newAst);
}

function preFilterStyles(html) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    const styleNodes = doc.querySelectorAll('style');
    styleNodes.forEach(style => {
        const originalCss = style.textContent;
        style.textContent = filterMermaidStyle(originalCss); // 替换为过滤后的内容
    });
    return doc.documentElement.outerHTML;
}
const customSanitizeHtml = (dirtyHtml) => {
    dirtyHtml = preFilterStyles(dirtyHtml);
    return sanitizeHtml(dirtyHtml, {
        // 允许的标签白名单保持不变（确保 SVG 相关标签都在）
        allowedTags: [
            'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'p', 'br', 'hr',
            'ul', 'ol', 'li', 'dl', 'dt', 'dd',
            'table', 'thead', 'tbody', 'tr', 'th', 'td',
            'blockquote', 'pre', 'code',
            'strong', 'em', 's', 'del', 'mark', 'kbd', 'sub', 'sup',
            'a', 'img',
            'div', 'span',
            '!--',
            'details', 'summary',
            'svg', 'g', 'path', 'circle', 'line', 'rect', 'text', 'marker',
            'defs', 'symbol', 'use', 'foreignobject', 'tspan', 'stop',
            'lineargradient', 'radialgradient', 'clippath', 'mask', 'pattern',
            'image', 'ellipse', 'polygon', 'polyline',
            'style',
            'iframe'
        ],
        allowVulnerableTags: true,
        // 关键修改：允许所有属性（但配合协议过滤和标签白名单，风险可控）
        allowedAttributes: {
            '*': ['*']
        },
        // 保留 URL 协议过滤，防止 javascript: 等恶意链接
        allowedSchemes: ['http', 'https', 'ftp', 'mailto', 'tel'],
        allowedSchemesByTag: {img: ['http', 'https', 'data']},
        allowedSchemesAppliedToAttributes: ['href', 'src', 'xlink:href'],

        // 完全放行 class
        allowedClasses: {'*': ['*']},

        // 禁用样式解析（浏览器环境必需）
        parseStyleAttributes: false,

        // 允许 HTML 注释
        allowComments: true,

        // 移除 script 标签
        transformTags: {
            'script': sanitizeHtml.simpleTransform('script', {}, {disallowedTagsMode: 'discard'}),
            '*': function (tagName, attribs) {
                for (let attr in attribs) {
                    if (attr.startsWith('on')) {
                        delete attribs[attr];
                    }
                }
                return {tagName, attribs};
            }
        }
    });
}

export default customSanitizeHtml;